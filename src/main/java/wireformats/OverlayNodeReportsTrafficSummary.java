package wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import node.MetricsData;

public class OverlayNodeReportsTrafficSummary implements Event {

	public int id;
	public int totalSent;
	public int totalRelayed;
	public long sentSummation;
	public int totalReceived;
	public long receivedSummation;
	
	
	// Construct from Message Node, specify port
	public OverlayNodeReportsTrafficSummary(int id, MetricsData metrics){
		
		this.id = id;
		this.totalSent 			= metrics.sendTracker;
		this.totalRelayed 		= metrics.relayTracker;
		this.sentSummation 		= metrics.sendSummation;
		this.totalReceived 		= metrics.receiveTracker;
		this.receivedSummation 	= metrics.receiveSummation;
	}
	
	// Construct from byte array received at registry
	public OverlayNodeReportsTrafficSummary(DataInputStream din) throws IOException {
		
		/*
		 * ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		 * DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		 */
		this.id = din.readInt();
		this.totalSent = din.readInt();
		this.totalRelayed = din.readInt();
		this.sentSummation = din.readLong();
		this.totalReceived = din.readInt();
		this.receivedSummation = din.readLong();
		
		
		// baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte getType() {
		return Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		dout.writeInt(id);
		dout.writeInt(totalSent);
		dout.writeInt(totalRelayed);
		dout.writeLong(sentSummation);
		dout.writeInt(totalReceived);
		dout.writeLong(receivedSummation);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

}
