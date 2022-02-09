package com.sfsutherland.p2p.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.sfsutherland.p2p.overlay.routing.RoutingEntry;
import com.sfsutherland.p2p.overlay.routing.RoutingTable;

public class RegistrySendsNodeManifest implements Event {

	public RoutingTable routingTable;
	public ArrayList<Integer> allSystemNodes;

	public RegistrySendsNodeManifest(RoutingTable table, ArrayList<Integer> allNodes) {
		this.routingTable = table;
		this.allSystemNodes = allNodes;
	}
	
	// Construct from byte array recieved at Messaging Node
	public RegistrySendsNodeManifest(DataInputStream din) throws IOException {
		this.routingTable = new RoutingTable();
		this.allSystemNodes = new ArrayList<Integer>();
		
		int routingTableSize = din.readByte();
		
		for (int i = 0; i < routingTableSize; i++) {
			int nodeID = din.readInt();
			byte ipFieldLength = din.readByte();
			byte[] ipAddress = new byte[ipFieldLength];
			din.readFully(ipAddress);
			int port = din.readInt();
			
			RoutingEntry entry = new RoutingEntry(nodeID, ipAddress, port);
			this.routingTable.addEntry(entry);
		}
		
		byte numberOfSystemNodes = din.readByte();
		for (int i = 0; i < numberOfSystemNodes; i++) {
			this.allSystemNodes.add(din.readInt());
		}
		
	}
	
	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeByte(this.getType());
		byte tableSize = this.routingTable.getSize();
		dout.writeByte(tableSize);
		
		for (int i = 0; i < tableSize; i++) {
			RoutingEntry entry = this.routingTable.entryList.get(i);
			dout.writeInt(entry.nodeID);
			
			byte[] address = entry.address;
			dout.writeByte(address.length);
			dout.write(address);
			dout.writeInt(entry.port);
		}
		
		byte systemNodeCount = (byte) this.allSystemNodes.size();
		dout.writeByte(systemNodeCount);
		
		for (int i = 0; i < systemNodeCount; i++) {
			dout.writeInt(this.allSystemNodes.get(i));
		}
		
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}

	@Override
	public byte getType() {
		return Protocol.REGISTRY_SENDS_NODE_MANIFEST;
	}

}
