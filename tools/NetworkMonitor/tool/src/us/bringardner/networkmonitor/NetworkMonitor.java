package us.bringardner.networkmonitor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import processing.app.Editor;

public class NetworkMonitor implements processing.app.tools.Tool  {


	static boolean executedFromMain = false;
	private static InetAddress localAddress;

	public static InetAddress getLocalAddress() {
		if( localAddress == null ) {
			synchronized (NetworkMonitor.class) {
				if( localAddress == null ) {
					Socket socket = new Socket();
					try {
						socket.connect(new InetSocketAddress("congress.gov", 80));
						localAddress = socket.getLocalAddress();
					} catch (IOException e) {
						System.out.println("Can't get local address!!!!!!");
						System.out.println(e.getMessage());
						e.printStackTrace();
					}finally {
						try {
							socket.close();
						} catch (Exception e2) {
						}
					}
					
				}
			}
		}

		return localAddress;
	}

	public static void main(String[] args) {
		executedFromMain = true;
		NetworkMonitor monitor = new NetworkMonitor();
		monitor.init(null);
		monitor.run();
	}

	static  Editor editor;
	static  NetworkMonitorFrame frame;

	public String getMenuTitle() {
		return "NetworkMonitorV5";
	}

	public void init(Editor newEditor) {
		editor = newEditor;
	}

	public void initMonitor() {
		//System.out.println("Enter init static NetworkMonitor.editor="+NetworkMonitor.editor);

		if( NetworkMonitor.editor != null ) {
			NetworkMonitor.editor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					frame.running = false;
					frame.dispose();
					frame=null;
				}
			});
		}
		int port = 6000;
		InetAddress address;

		try {
			
			address = getLocalAddress();

		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(NetworkMonitor.editor, e, "Could not bet localhost address. Unable to run NetworkMonitor at this time.", JOptionPane.ERROR_MESSAGE);
			return;
		}

		//  Find an open port
		try {
			DatagramSocket sock = new DatagramSocket(port,address);							
			frame = new NetworkMonitorFrame(sock);
		} catch (Exception e) {
			NetworkMonitorConfigDialog d = new NetworkMonitorConfigDialog();
			DatagramSocket sock = d.showDialog("Could not connect to default port:\n"+e.getMessage(),null);
			if( sock == null ) {
				return;
			}
			frame = new NetworkMonitorFrame(sock);
		}
		frame.setLocationRelativeTo(null);
		if( editor != null) {
			frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);				
		} else {
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		}



	}


	public void run() {			
		if( frame == null ) {
			initMonitor();
		}
		if( frame != null ) {
			try {
				frame.setVisible(true);			
			} catch (Throwable e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(NetworkMonitor.editor, e, "Unable to run Network monitor at this time", JOptionPane.ERROR_MESSAGE);
			} 
		}

	}



}
