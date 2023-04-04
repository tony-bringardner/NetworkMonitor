package us.bringardner.neworkmonitor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class NetworkMonitorConfigDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private DatagramSocket validatedPort=null;
	private JTextArea messageTextArea;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NetworkMonitorConfigDialog dialog = new NetworkMonitorConfigDialog();
			DatagramSocket port = dialog.showDialog("",null);
			System.out.println("Port "+port);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DatagramSocket showDialog(String msg,DatagramSocket current) {
		
		if( msg != null && !msg.trim().isEmpty()) {
			messageTextArea.setText(msg);
		} else {
			messageTextArea.setVisible(false);
		}
		
		if( current != null ) {
			textField.setText(""+current.getLocalPort());				
		}
		
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);
		
		return validatedPort;
	}
	
	/**
	 * Create the dialog.
	 */
	public NetworkMonitorConfigDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		
		JLabel lblNewLabel_1 = new JLabel("Network monitor will listen for activity on");
		lblNewLabel_1.setBounds(77, 82, 279, 16);
		contentPanel.add(lblNewLabel_1);
		String title = "Local host ";
		try {
			title = "Local host ("+InetAddress.getLocalHost().getHostAddress()+")";
			JLabel lblNewLabel_2 = new JLabel(title);
			lblNewLabel_2.setBounds(109, 110, 214, 16);
			contentPanel.add(lblNewLabel_2);
			textField = new JTextField();
			textField.setBounds(212, 141, 58, 26);
			contentPanel.add(textField);
			textField.setHorizontalAlignment(SwingConstants.RIGHT);
			textField.setText("port");
			textField.setColumns(10);
			
			JLabel lblNewLabel = new JLabel("UDP Port");
			lblNewLabel.setBounds(135, 146, 55, 16);
			contentPanel.add(lblNewLabel);
			
			messageTextArea = new JTextArea();
			messageTextArea.setDisabledTextColor(SystemColor.activeCaptionText);
			messageTextArea.setEnabled(false);
			messageTextArea.setEditable(false);
			messageTextArea.setLineWrap(true);
			messageTextArea.setBounds(48, 6, 368, 55);
			contentPanel.add(messageTextArea);
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					JButton okButton = new JButton("OK");
					okButton.addActionListener(new ActionListener() {
						int port = -1;
						public void actionPerformed(ActionEvent e) {														
							try {
								port = Integer.parseInt(textField.getText());
								InetAddress address = InetAddress.getLocalHost();
								DatagramSocket sock = new DatagramSocket(port,address);							
								validatedPort = sock;
								dispose();
							} catch (Exception e1) {
								//e1.printStackTrace();
								JOptionPane.showMessageDialog(NetworkMonitorConfigDialog.this, e1, "Could not connect to "+port, JOptionPane.ERROR_MESSAGE);
							}
						}
					});
					okButton.setActionCommand("OK");
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
				}
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							validatedPort=null;
							dispose();
						}
					});
					cancelButton.setActionCommand("Cancel");
					buttonPane.add(cancelButton);
				}
			}
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, e1, "Could not bet localhost address. Unable to run NetworkMonitor at this time.", JOptionPane.ERROR_MESSAGE);
			
		}
		
	}
}
