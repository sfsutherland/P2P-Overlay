package transport;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPServerThread implements Runnable {
	public int port;
	private ServerSocket serverSocket;
	private static Logger LOG = LogManager.getLogger( TCPServerThread.class);
	private TCPConnectionCache cc;
	
	
	public TCPServerThread(TCPConnectionCache cc) {
		this(1200, cc);
	}
	
	public TCPServerThread(int port, TCPConnectionCache cc) {
		this.port = port;
		this.cc = cc;
	}
	
	private boolean bindSuccess() throws IOException {
		try {
			serverSocket.bind(new InetSocketAddress(this.port));
			return true;
		}
		catch (BindException e) {
			LOG.debug("BindException for port " + this.port);
			return false;
		}
	}
	
	@Override
	public void run() {
		
		try {
			serverSocket = new ServerSocket();
		} catch(IOException e) {
			LOG.fatal("Creating ServerSocket");
			System.exit(1);
		}
		
		try {
			while (!bindSuccess()) {
				this.port++;
			}
		} catch (IOException e) {
			LOG.fatal("Binding to port");
			System.exit(1);
		}
		LOG.debug("TCP Server bound at port " + this.port);
		cc.node.setPortBound(true);
		
		while(true) {
			try {
				// Block on incoming connections.
				LOG.debug("Waiting for a connection to accept");
				Socket incomingConnectionSocket = serverSocket.accept();
				
				LOG.debug("Spawning receiver thread...");
				Thread receiver = new Thread(new TCPReceiverThread(incomingConnectionSocket, cc));
				receiver.start();
		
				} catch(IOException e) {
					LOG.fatal("Accepting connections", e);
					System.exit(1);
				}
			}
		
	}
}
