package com.sfsutherland.p2p.overlay.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.node.OverlayConnectionService;
import com.sfsutherland.p2p.overlay.wireformats.Event;
import com.sfsutherland.p2p.overlay.wireformats.RegistryReportsRegistrationStatus;

public class RegistrationResponseHandler implements Handler {
	
	private static Logger LOG = LogManager.getLogger( RegistrationResponseHandler.class);
	private OverlayConnectionService connectionService;

	public RegistrationResponseHandler(OverlayConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		RegistryReportsRegistrationStatus message = (RegistryReportsRegistrationStatus) e;
		
		int assignedID = message.assignedID;
		if (assignedID == -1) {
			LOG.fatal("Registration failed");
			System.exit(1);
		}
		else connectionService.setUniqueID(message.assignedID);
		
		LOG.debug("Received registration ID of " + assignedID + " and message string: " + message.infoString);
		
		LOG.info("Successfully registered");

	}

}
