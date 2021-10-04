package wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryReportsDeregistrationStatus implements Event {
	
	public int status;
	public String infoString;

	// Construct at registry, specifying a new ID (or -1) and the info string
	public RegistryReportsDeregistrationStatus(int status, String infoString) {
		this.status = status;
		this.infoString = infoString;
	}
	
	// Construct from byte array recieved at Messaging Node
	public RegistryReportsDeregistrationStatus(DataInputStream din) throws IOException {
		
		/*
		 * ByteArrayInputStream baInputStream = new
		 * ByteArrayInputStream(marshalledBytes); DataInputStream din = new
		 * DataInputStream(new BufferedInputStream(baInputStream));
		 */
		this.status = din.readInt();
		byte infoStringLength = din.readByte();
		byte[] infoStringByteArray = new byte[infoStringLength];
		din.readFully(infoStringByteArray);
		this.infoString = new String (infoStringByteArray);
		
		//baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		dout.writeInt(this.status);
		dout.writeByte(this.infoString.length());
		dout.write(this.infoString.getBytes());
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

	@Override
	public byte getType() {
		return Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS;
	}

}