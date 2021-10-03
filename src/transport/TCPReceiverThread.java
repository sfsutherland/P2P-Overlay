package transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.Node;
import wireformats.EventFactory;
import wireformats.Protocol;
import wireformats.Event;

public class TCPReceiverThread implements Runnable {
	
	private Logger LOG = LogManager.getLogger(TCPReceiverThread.class);
	private Socket socket;
	private Node ownerNode;
	
	public TCPReceiverThread(Socket s, TCPConnectionCache cc) {
		this.socket = s;
		this.ownerNode = cc.node;
	}

	@Override
	public void run() {

		DataInputStream inputStream = null;
		
		try {
			inputStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			LOG.fatal("initializing socket DataInputStream");
			System.exit(1);
		}
		
		while(true) {
			try {
				byte messageType = inputStream.readByte();
				
				LOG.debug("Receiving " + Protocol.typeString(messageType) + " Event from connection...");
	
				
				EventFactory factory = EventFactory.getInstance();
				Event event = null;
				try {
					event = factory.createEvent(messageType, inputStream, socket.getInetAddress().getHostAddress());
				} catch (IOException e) {
					LOG.fatal("Neighbor socket stream has closed. Exiting thread");
					this.socket.close();
					return;
				}
				ownerNode.onEvent(event);
				
			} catch (IOException e) { // when remote end of socket exits the connection
				try {
					socket.close();
				} catch (IOException e1) {
					LOG.fatal("Could not close socket!");
					System.exit(1);
				}
				return;
			}
		}
		
	}

}
