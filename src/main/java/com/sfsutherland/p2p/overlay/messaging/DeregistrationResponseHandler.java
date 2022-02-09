package com.sfsutherland.p2p.overlay.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.wireformats.Event;
import com.sfsutherland.p2p.overlay.wireformats.Protocol;
import com.sfsutherland.p2p.overlay.wireformats.RegistryReportsDeregistrationStatus;

public class DeregistrationResponseHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( DeregistrationResponseHandler.class);

	@Override
	public void handle(Event e) {
		RegistryReportsDeregistrationStatus message = (RegistryReportsDeregistrationStatus) e;
		LOG.debug("Received " + Protocol.typeString(e.getType()) + " from registry");
		LOG.debug("Deregistration status: " + message.status + "(" + message.infoString + ")");
		System.exit(0);
	}

}
