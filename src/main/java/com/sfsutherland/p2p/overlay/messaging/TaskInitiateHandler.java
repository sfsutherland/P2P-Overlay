package com.sfsutherland.p2p.overlay.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.node.OverlayConnectionService;
import com.sfsutherland.p2p.overlay.wireformats.Event;
import com.sfsutherland.p2p.overlay.wireformats.OverlayNodeReportsTaskFinished;
import com.sfsutherland.p2p.overlay.wireformats.RegistryRequestsTaskInitiate;

public class TaskInitiateHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( TaskInitiateHandler.class);
	private OverlayConnectionService connectionService;

	public TaskInitiateHandler(OverlayConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		RegistryRequestsTaskInitiate message = (RegistryRequestsTaskInitiate) e;
		System.out.println("Starting message transfer with " + message.numberOfMessages + " messages");

		for (int i = 0; i < message.numberOfMessages; i++) {
			connectionService.sendRandomDataMessage();;
		}
		connectionService.reportTaskFinished();
	}
	

	
	
}
