package us.bringardner.networkmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class NetworkMonitorFrame extends JFrame implements Runnable {


	public static interface DebugListner {
		public void receive(String msg);
	}

	private  Map<String,NetowrkMonitorPanel> listners = new TreeMap<String, NetowrkMonitorPanel>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAXUDPLEN = 1024;


	private JPanel contentPane;
	DatagramSocket sock ;
	private int timeout=30000;
	public boolean running;
	private boolean stopping = false;

	private JTabbedPane tabbedPane;
	private JButton btnNewButton_1;

	private SocketAddress sa;

	private JTextField adminPortTextField;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					InetAddress address = NetworkMonitor.getLocalAddress();
					DatagramSocket sock = new DatagramSocket(6000,address);
					NetworkMonitorFrame frame = new NetworkMonitorFrame(sock);
					frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public void run () {

		running = true;
		actionPing();
		
		while( running && !stopping) {
			DatagramPacket recPckt = new DatagramPacket(new byte[MAXUDPLEN],MAXUDPLEN);
			try {
				sock.receive(recPckt);

				int len = recPckt.getLength();
				if( len > 0 ) {
					final String client = recPckt.getAddress().toString();					
					String msg = new String(recPckt.getData(),0,len);
					NetowrkMonitorPanel l = listners.get(client);
					if( l == null ) {
						l = new NetowrkMonitorPanel(this,recPckt.getAddress());
						listners.put(client, l);
						final NetowrkMonitorPanel ll = l;
						SwingUtilities.invokeLater(new Runnable() {								
							//@Override
							public void run() {
								tabbedPane.addTab(client,  ll);	
								int idx = tabbedPane.getComponentCount()-1;
								tabbedPane.setForegroundAt(idx, Color.black);
								tabbedPane.setBackgroundAt(idx, Color.white);

							}
						});
					} 
					l.receive(msg);
				}
			} catch (java.net.SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
				for(DebugListner l : listners.values()) {
					l.receive(e.getMessage());
				}
				if( sock.isClosed()) {
					running = false;
				}
			}	
		}
		//System.out.println("running = "+running+" stopping"+stopping);
		try {
			sock.close();	
		} catch (Exception e) {
		}
		running = false;
		for(DebugListner l : listners.values()) {
			l.receive("stop");
		}
		//System.out.println("Exit run running = "+running+" stopping"+stopping);
		
	}

	public void send(String msg, InetAddress address) throws IOException {

		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length(),address, sock.getLocalPort());
		sock.send(sendPacket);
	}




	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws SocketException 
	 */
	public NetworkMonitorFrame(DatagramSocket socket)  {
		sock = socket;
		try {
			sock.setSoTimeout(timeout);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent e) {

			}

			public void windowIconified(WindowEvent e) {

			}

			public void windowDeiconified(WindowEvent e) {

			}

			public void windowDeactivated(WindowEvent e) {

			}

			public void windowClosing(WindowEvent e) {

			}

			public void windowClosed(WindowEvent e) {
				try {
					running = false;
					Thread.yield();
					sock.close();
				} catch (Exception e2) {
				}

			}

			public void windowActivated(WindowEvent e) {

			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1263, 836);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel northPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) northPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(northPanel, BorderLayout.NORTH);
		
		JButton btnNewButton = new JButton("Ping");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionPing();
			}
		});
		northPanel.add(btnNewButton);

		btnNewButton_1 = new JButton("Change Port");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionChangePort();
			}
		});
		
		adminPortTextField = new JTextField();
		adminPortTextField.setText("224.0.0.252:60000");
		adminPortTextField.setBorder(new TitledBorder(null, "Admin Port", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		northPanel.add(adminPortTextField);
		adminPortTextField.setColumns(15);
		northPanel.add(btnNewButton_1);

		setTitle("Network Monitor availible at "+sock.getLocalSocketAddress());
		new Thread(this).start();
	}

	private void showError(Exception e,String msg) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(this, e.getMessage(), msg, JOptionPane.ERROR_MESSAGE);
	}


	public int getAdminPort() {
		int ret = 60000;
		try {
			String tmp = adminPortTextField.getText();
			int idx = tmp.indexOf(":");
			if( idx>0) {
				tmp = tmp.substring(idx+1);
			}
			ret = Integer.parseInt(tmp);
		} catch (Exception e) {
			//System.out.println(e);
			showError(e, "Can't parse admin port");
		}
		return ret;
	}
	
	public InetAddress getAdminAddress() {
		InetAddress ret = null;
		try {
			String tmp = adminPortTextField.getText();
			int idx = tmp.indexOf(":");
			if( idx>0) {
				tmp = tmp.substring(0,idx);
			}
			ret = InetAddress.getByName(tmp);
			
		} catch (Exception e) {
			showError(e, "Can't parse admin address.");
		}
		return ret;
	}
	
	protected void braodcast(String msg) {
		
		try {
			int adminPort = getAdminPort();
			MulticastSocket socket = new MulticastSocket(adminPort);
			socket.setBroadcast(true);
			InetSocketAddress group = new InetSocketAddress(getAdminAddress(),adminPort);
			
	        socket.joinGroup(group,null);
	        byte [] data = msg.getBytes();
	        DatagramPacket packet = new DatagramPacket(data, data.length,group);	        
	        socket.send(packet); 	        
	        socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void actionPing() {				
	        String  buf = "ping "+NetworkMonitor.getLocalAddress().toString().substring(1)+" "+sock.getLocalPort();
	        new Thread(new Runnable() {
				
				@Override
				public void run() {
					braodcast(buf);					
				}
			}).start();
	}


	protected void actionChangePort() {
		NetworkMonitorConfigDialog d = new NetworkMonitorConfigDialog();
		//System.out.println("sa="+sa);
		//System.out.println(sock.getLocalSocketAddress());
		DatagramSocket s = d.showDialog("",sock);
		
		if( s != null ) {			
			try {
				sock.close();
			} catch (Exception e) {
			}
			while( running) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			sock = s;
			setTitle("Network Monitor availible at "+sock.getLocalSocketAddress());
			sa = sock.getLocalSocketAddress();
			new Thread(this).start();
		}

	}

}
