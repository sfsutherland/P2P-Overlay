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
	
	public RoutingEntry routeTo(int id) {
		RoutingEntry before = this.entryList.get(0);
		for (RoutingEntry re : this.entryList) {
			if (re.nodeID == id)
				return re;
			
			else if (before.nodeID == re.nodeID)
				continue;
			
			else if ((before.nodeID < id) && (id < re.nodeID))
				return before;
			
			else if ( (re.nodeID < before.nodeID) && (before.nodeID < id))
				return before;
			
			else if ((id < re.nodeID) && (re.nodeID < before.nodeID))
				return before;
			
			else
				before = re;
		}
		return before;
	}

}
