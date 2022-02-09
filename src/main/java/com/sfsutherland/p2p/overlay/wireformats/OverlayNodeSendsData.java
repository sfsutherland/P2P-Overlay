package com.sfsutherland.p2p.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class OverlayNodeSendsData implements Event {

	public int destinationID;
	public int sourceID;
	public int payload;
	public ArrayList<Integer> trace;	
	
	// Construct from Message Node, specify port
	public OverlayNodeSendsData(int destID, int sourceID, int payload){
		this.destinationID = destID;
		this.sourceID = sourceID;
		this.payload = payload;
		this.trace = new ArrayList<Integer>();
	}
	
	public void addHop(int hopId) {
		this.trace.add(hopId);
	}
	
	// Construct from byte array received at registry
	public OverlayNodeSendsData(DataInputStream din) throws IOException {
		
		/*
		 * ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		 * DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		 */
		this.trace = new ArrayList<Integer>();
		this.destinationID = din.readInt();
		this.sourceID = din.readInt();
		this.payload = din.readInt();
		int hopSize = din.readInt();
		for (int i = 0; i < hopSize; i++) {
			this.trace.add(din.readInt());
		}
		
		// baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte getType() {
		return Protocol.OVERLAY_NODE_SENDS_DATA;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		dout.writeInt(this.destinationID);
		dout.writeInt(this.sourceID);
		dout.writeInt(this.payload);
		for (int i = 0; i < this.trace.size(); i++)
			dout.writeInt(this.trace.get(i));
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

}
