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
			byte[] marshalledBytes = event.getBytes();
	
			int messageLength = marshalledBytes.length;
			outputStream.write(marshalledBytes, 0, messageLength);
		}
		catch (IOException e) {
			LOG.fatal("sending message");
			System.exit(1);
		}
		LOG.debug("Sent " + Protocol.typeString(event.getType()) + " message to port " + socket.getPort() + " at " + socket.getInetAddress().getHostAddress());
		
	}

}






