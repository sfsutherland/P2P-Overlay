package transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wireformats.Event;
import wireformats.Protocol;



public class TCPSender {
	
	private Socket socket;
	private static Logger LOG = LogManager.getLogger( TCPSender.class);
	
	public TCPSender(Socket s) {
		if (s.isClosed()) {
			LOG.fatal("Trying to send over a closed socket");
			System.exit(1);
		}
		this.socket = s;
	}
	
	public TCPSender(Socket s, Event event) {
		this.socket = s;
		sendMessage(event);
	}
	
	public void sendMessage(Event event) {
		DataOutputStream outputStream = null;
		
		try {
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			LOG.fatal("initializing socket data streams", e);
			System.exit(1);
		}
		
		try {
			/*
			 * LOG.debug("Socket is connected: " + (this.socket.isConnected() ? "true" : "false"));
			 * LOG.debug("Socket is inputShutdown: " + (this.socket.isInputShutdown() ? "true" : "false"));
			 * LOG.debug("Socket is outputShutdown: " + (this.socket.isOutputShutdown() ? "true" : "false"));
			 * LOG.debug("Socket is bound: " + (this.socket.isBound() ? "true" : "false"));
			 * LOG.debug("Socket ports: " + this.socket.getLocalPort() + " ---> " + this.socket.getPort());
			 * LOG.debug("Socket IPs: " + this.socket.getLocalAddress().getHostAddress() + " ---> " + this.socket.getInetAddress().getHostAddress());
			 */
			
			
			// send a message
			byte[] marshalledBytes = event.getBytes();
	
			int messageLength = marshalledBytes.length;
			outputStream.write(marshalledBytes, 0, messageLength);
			//LOG.debug("Sending " + messageLength + " byte message");
			//LOG.debug("Have written " + outputStream.size() + " bytes into output stream.");
		}
		catch (IOException e) {
			LOG.fatal("sending message");
			System.exit(1);
		}
		LOG.debug("Sent " + Protocol.typeString(event.getType()) + " message to port " + socket.getPort() + " at " + socket.getInetAddress().getHostAddress());
		
	}

}






