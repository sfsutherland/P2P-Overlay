package com.sfsutherland.p2p.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OverlayNodeReportsTaskFinished implements Event {

	public int port;
	private byte[] network_address;
	public String messageIPString;
	public int id;
	private static Logger LOG = LogManager.getLogger(OverlayNodeReportsTaskFinished.class);
	
	
	// Construct from Message Node, specify port
	public OverlayNodeReportsTaskFinished(int port, int id){
		
		this.port = port;
		this.id = id;
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
	public OverlayNodeReportsTaskFinished(DataInputStream din) throws IOException {
		
		/*
		 * ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		 * DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		 */
		byte addressLength = din.readByte();
		byte[] address = new byte[addressLength];
		din.readFully(address);
		this.port = din.readInt();
		this.id = din.readInt();
		
		this.network_address = address;
		this.messageIPString = InetAddress.getByAddress(network_address).getHostAddress();
		
		// baInputStream.close();
		//din.close();
	}
	
	@Override
	public byte getType() {
		return Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED;
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
		dout.writeInt(this.id);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}
}
