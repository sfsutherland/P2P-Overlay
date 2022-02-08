package com.sfsutherland.p2p.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryRequestsTaskInitiate implements Event {

	public int numberOfMessages;

	// Construct at registry, specifying a new ID (or -1) and the info string
	public RegistryRequestsTaskInitiate(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}
	
	// Construct from byte array recieved at Messaging Node
	public RegistryRequestsTaskInitiate(DataInputStream din) throws IOException {
		
		/*
		 * ByteArrayInputStream baInputStream = new
		 * ByteArrayInputStream(marshalledBytes); DataInputStream din = new
		 * DataInputStream(new BufferedInputStream(baInputStream));
		 */
		this.numberOfMessages = din.readInt();
		
		//baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		dout.writeInt(this.numberOfMessages);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

	@Override
	public byte getType() {
		return Protocol.REGISTRY_REQUESTS_TASK_INITIATE;
	}


}
