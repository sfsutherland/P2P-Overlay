package com.sfsutherland.p2p.overlay.node;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricsDataTest {
	
	MetricsData metrics;
	private final PrintStream originalOut = System.out;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	public MetricsDataTest() {
		metrics = new MetricsData();
		metrics.receiveSummation = 0;
		metrics.receiveTracker = 0;
		metrics.relayTracker = 0;
		metrics.sendSummation = 0l;
		metrics.sendTracker = 0;
	}
	
	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(outContent));
	}
	
	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}
	
 	@Test
	void testPrintCountersAndDiagnostics() {
				
		
		String expectedString = "|ID\t\t|Packets Sent\t|Packets Received\t|Packets Relayed\t|Sum Values Sent\t|Sum Values Received|\n\n"+
		"| Totals\t|" +0 +"\t\t|" +0+ "\t\t\t|"+ 0+ "\t\t\t|" +0+ "\t|" +0 + "|\n" ;
 		metrics.printCountersAndDiagnostics();
		assertEquals(expectedString, outContent.toString());
	}

}
