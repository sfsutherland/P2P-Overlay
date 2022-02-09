package com.sfsutherland.p2p.overlay.routing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoutingTableTest {
	
	RoutingTable routingTable;
	
	public RoutingTableTest() {
		this.routingTable = new RoutingTable();
		routingTable.addEntry(new RoutingEntry(119, new byte[4], 0));
		routingTable.addEntry(new RoutingEntry(4, new byte[4], 0));
		routingTable.addEntry(new RoutingEntry(19, new byte[4], 0));
	}

	@Test
	void testRouteTo82Returns19() {
		RoutingEntry routingEntry = routingTable.routeTo(82);
		assertEquals(routingEntry.nodeID, 19 );
	}
	
	@Test
	void testRouteTo19Returns19() {
		RoutingEntry routingEntry = routingTable.routeTo(19);
		assertEquals(routingEntry.nodeID, 19 );
	}
	
	@Test
	void testRouteTo119Returns119() {
		RoutingEntry routingEntry = routingTable.routeTo(119);
		assertEquals(routingEntry.nodeID, 119 );
	}

	@Test
	void testRouteTo4Returns4() {
		RoutingEntry routingEntry = routingTable.routeTo(4);
		assertEquals(routingEntry.nodeID, 4 );
	}
	
	@Test
	void testRouteTo18Returns4() {
		RoutingEntry routingEntry = routingTable.routeTo(18);
		assertEquals(routingEntry.nodeID, 4 );
	}
	
	@Test
	void testRouteTo33Returns19() {
		RoutingEntry routingEntry = routingTable.routeTo(33);
		assertEquals(routingEntry.nodeID, 19 );
	}
	
	@Test
	void testRouteTo60Returns19() {
		RoutingEntry routingEntry = routingTable.routeTo(60);
		assertEquals(routingEntry.nodeID, 19 );
	}
}
