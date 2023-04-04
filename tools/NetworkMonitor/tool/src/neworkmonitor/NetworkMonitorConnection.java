package us.bringardner.neworkmonitor;

import java.io.IOException;

public interface NetworkMonitorConnection {
	
	public String getName();
	public void close();
	public void addReceiver(NetworkMonitorReceiver reciever);
	public void send(String msg) throws IOException;
	
}
