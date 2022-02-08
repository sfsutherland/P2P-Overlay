package com.sfsutherland.p2p.overlay.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.wireformats.Event;

public abstract class Node {
	
	private static Logger LOG = LogManager.getLogger( Node.class);
	
	public abstract void handleEvent(Event e);

	public abstract void handleUserInput(String s);
	
	static void usageErrorExit(String s) {
		LOG.info("ERROR: " + s);
		System.exit(0);
	}

}