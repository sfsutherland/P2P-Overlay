package com.sfsutherland.p2p.overlay.routing;

import java.util.ArrayList;

public class RoutingTable {
	
	public ArrayList<RoutingEntry> entryList;
	public final int ownerID;
	
	public RoutingTable() {
		this(-1);
	}
	
	public RoutingTable(int ownerID) {
		this.ownerID = ownerID;
		this.entryList = new ArrayList<RoutingEntry>();
	}
	
	public void addEntry(RoutingEntry entry) {
		this.entryList.add(entry);
	}
	
	public byte getSize() {
		return (byte) this.entryList.size();
	}
	
	public RoutingEntry routeTo(int destinationID) {
		
		RoutingEntry before = this.entryList.get(0);
		
		for (RoutingEntry routingEntry : this.entryList) {
			
			if (routingEntry.nodeID == destinationID)
				return routingEntry;
			
			else if (before.nodeID == routingEntry.nodeID)
				continue;
			
			else if ((before.nodeID < destinationID) && (destinationID < routingEntry.nodeID))
				return before;
			
			else if ( (routingEntry.nodeID < before.nodeID) && (before.nodeID < destinationID))
				return before;
			
			else if ((destinationID < routingEntry.nodeID) && (routingEntry.nodeID < before.nodeID))
				return before;
			
			else
				before = routingEntry;
		}
		return before;
	}

}
