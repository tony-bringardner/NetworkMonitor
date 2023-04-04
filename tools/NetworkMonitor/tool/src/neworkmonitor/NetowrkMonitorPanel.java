package us.bringardner.neworkmonitor;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

public class NetowrkMonitorPanel extends JPanel implements NetworkMonitorFrame.DebugListner {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField commandTextField;
	private NetworkMonitorFrame parent;
	private InetAddress client;
	private JTextArea textArea;
	private JCheckBox showTimeCheckBox;
	private JSpinner maxLinesSpinner;
	private PlotterPanel plotter=new PlotterPanel();
	private int previousPosition=0;

	private List<String> previousCommands = new ArrayList<String>();
	private boolean needDate = true;



	//private ChartPanel chart = new ChartPanel();


	/**
	 * Create the panel.
	 */
	public NetowrkMonitorPanel(NetworkMonitorFrame parent, InetAddress client) {
		this.parent = parent;
		this.client = client;
		setLayout(new BorderLayout(0, 0));

		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);

		commandTextField = new JTextField();
		commandTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSend();
			}
		});

		commandTextField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				int code = e.getExtendedKeyCode();
				if( previousCommands.size()>0) {
					if( code == 38) {
						//up
						if(previousPosition> 0 ) {
							commandTextField.setText(previousCommands.get(--previousPosition));
						}
					} else if( code == 40) {
						int sz = previousCommands.size();
						if(previousPosition< (sz-1) ) {
							commandTextField.setText(previousCommands.get(++previousPosition));
						} else {
							commandTextField.setText("");
						}
					}
				}
			}
		});
		northPanel.setLayout(new BorderLayout(0, 0));

		northPanel.add(commandTextField);
		commandTextField.setColumns(60);

		JButton btnNewButton1 = new JButton("Send");
		btnNewButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSend();
			}
		});
		northPanel.add(btnNewButton1, BorderLayout.EAST);

		{
			splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.5);




			JScrollPane scrollPane = new JScrollPane();	
			textArea = new JTextArea();
			scrollPane.setViewportView(textArea);
			splitPane.setLeftComponent(scrollPane);
			splitPane.setRightComponent(null);
			add(splitPane, BorderLayout.CENTER);
		}
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.WEST);
		
		autoScrollCheckBox = new JCheckBox("Autoscroll");
		panel_1.add(autoScrollCheckBox);
		autoScrollCheckBox.setSelected(true);
		
				
				showTimeCheckBox = new JCheckBox("Show timestamp");
				panel_1.add(showTimeCheckBox);
				
				JPanel panel_2 = new JPanel();
				panel.add(panel_2, BorderLayout.EAST);
				
				lineEndingComboBox = new JComboBox<String>();
				lineEndingComboBox.setModel(new DefaultComboBoxModel<String>(lineEndings));
				lineEndingComboBox.setSelectedIndex(3);
				panel_2.add(lineEndingComboBox);
						
								maxLinesSpinner = new JSpinner();
								panel_2.add(maxLinesSpinner);
								maxLinesSpinner.setBorder(new TitledBorder(null, "Max Lines", TitledBorder.LEADING, TitledBorder.TOP, null, null));
								maxLinesSpinner.setModel(new SpinnerNumberModel(200,50,1000,1));
				
						JButton btnNewButton = new JButton("Clear output");
						panel_2.add(btnNewButton);
						
						JPanel panel_3 = new JPanel();
						panel.add(panel_3, BorderLayout.CENTER);
						
						btnNewButton_2 = new JButton("Open");
						panel_3.add(btnNewButton_2);
						
						acquireButton = new JButton("Acquire");
						panel_3.add(acquireButton);
						
								showPlotterCheckBox = new JCheckBox("Show Plotter");
								panel_3.add(showPlotterCheckBox);
								showPlotterCheckBox.addChangeListener(new ChangeListener() {
									public void stateChanged(ChangeEvent e) {
										actionShowPlotter();
									}
								});
						acquireButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								actionAcquire();
							}
						});
						btnNewButton_2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								actionOpen();
							}
						});
						btnNewButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								actionClear();
							}
						});

	}

	
	protected void actionAcquire() {
			String  buf = "acquire "+NetworkMonitor.getLocalAddress().toString().substring(1)+" "+parent.sock.getLocalPort()+" "+client.getHostAddress();
	        parent.braodcast(buf);		
	}

	protected void actionOpen() {
		String url = "http://"+client.getHostAddress();
		
		
		try {
			if(Desktop.isDesktopSupported()){
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new URI(url));
			} else {
				Runtime.getRuntime().exec("xdg-open " + url);			
			}
		} catch (URISyntaxException | IOException e) {
			showError(e,"Can not lunch browser. url="+url);
		}
	
}

	private void showError(Exception e,String msg) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(this, e.getMessage(), msg, JOptionPane.ERROR_MESSAGE);
	}

	protected void actionShowPlotter() {
		if( showPlotterCheckBox.isSelected()) {
			splitPane.setRightComponent(plotter);
		} else {
			splitPane.setRightComponent(null);
		}		
	}

	protected void actionClear() {
		textArea.setText("");

	}

	String [] lineEndings = new String[] {"No line ending", "Newline", "Carrage Return", "Both NL & CR"};
	
	protected void actionSend() {
		try {
			String val = commandTextField.getText();
			previousCommands.add(val);
			previousPosition = previousCommands.size();
			if(previousPosition>200) {
				previousCommands.remove(0);
				previousPosition = previousCommands.size();
			}
			String end = "";
			switch (lineEndingComboBox.getSelectedIndex()) {
			case 1:end = "\n";break;
			case 2:end = "\r";break;
			case 3:end = "\r\n";break;
			default:
				break;
			}
			parent.send(val+end, client);
			commandTextField.setText("");
		} catch (IOException e) {
			showError(e, "Error sending data");
		}
	}
	//10:14:13.761 -> 
	SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS -> ");
	private JSplitPane splitPane;
	private JCheckBox showPlotterCheckBox;
	
	private JButton btnNewButton_2;
	private JButton acquireButton;
	private JCheckBox autoScrollCheckBox;
	private JComboBox<String> lineEndingComboBox;



	public void receive(String msg) {
		if( showTimeCheckBox.isSelected() && needDate) {
			textArea.append(fmt.format(new Date()));
		}
		
		textArea.append(msg);
	

		int max = (Integer) maxLinesSpinner.getValue();
		while( textArea.getLineCount() > max ) {
			try {			
				textArea.getDocument().remove(0, textArea.getLineEndOffset(1));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		if( autoScrollCheckBox.isSelected()) {
			textArea.setCaretPosition(textArea.getDocument().getEndPosition().getOffset()-1);
		}
		
		if( plotter != null ) {
			plotter.receive(msg);
		}

		 
	}
}
