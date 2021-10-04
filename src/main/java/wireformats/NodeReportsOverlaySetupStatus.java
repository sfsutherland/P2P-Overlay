package wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeReportsOverlaySetupStatus implements Event {

	public int status;
	public String infoString;
	
	
	// Construct from Message Node, specify port
	public NodeReportsOverlaySetupStatus(int status, String infoString){
		
		this.status = status;
		this.infoString = infoString;
	}
	
	// Construct from byte array received at registry
	public NodeReportsOverlaySetupStatus(DataInputStream din) throws IOException {		
		/*
		 * ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		 * DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		 */
		this.status = din.readInt();
		byte messageLength = din.readByte();
		
		byte[] infoStringBytes = new byte[messageLength];
		din.readFully(infoStringBytes);
		this.infoString = new String(infoStringBytes);
		
		// baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte getType() {
		return Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS;
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

}
