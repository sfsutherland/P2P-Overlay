package wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OverlayNodeSendsRegistration implements Event {
	public int port;
	private byte[] network_address;
	public String messageIPString;
	public String fromIP;
	private static Logger LOG = LogManager.getLogger(OverlayNodeSendsRegistration.class);
	
	
	// Construct from Message Node, specify port
	public OverlayNodeSendsRegistration(int port){
		
		this.fromIP = null;
		
		this.port = port;
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			LOG.debug("Unable to resolve host ip");
			System.exit(1);
		}
		this.network_address = localAddress.getAddress();
		this.messageIPString = localAddress.getHostAddress();
	}
	
	// Construct from byte array received at registry
	public OverlayNodeSendsRegistration(DataInputStream din, String fromIP) throws IOException {
		this.fromIP = fromIP;
		
		/*
		 * ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		 * DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		 */
		byte addressLength = din.readByte();
		byte[] address = new byte[addressLength];
		din.readFully(address);
		this.port = din.readInt();
		this.network_address = address;
		this.messageIPString = InetAddress.getByAddress(network_address).getHostAddress();
		
		// baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte getType() {
		return Protocol.OVERLAY_NODE_SENDS_REGISTRATION;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		dout.writeByte(this.network_address.length);
		dout.write(this.network_address);
		dout.writeInt(this.port);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

}