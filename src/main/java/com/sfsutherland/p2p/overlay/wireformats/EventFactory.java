package com.sfsutherland.p2p.overlay.wireformats;

import java.io.DataInputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventFactory {
	
	private static Logger LOG = LogManager.getLogger(EventFactory.class);
	public static final EventFactory instance = new EventFactory();
	
	private EventFactory() {}
	
	public static EventFactory getInstance() {
		return instance;
	}
	
	public Event createEvent(byte type, DataInputStream din, String receivedFrom) throws IOException {
		switch(type) {
		
		case Protocol.OVERLAY_NODE_SENDS_REGISTRATION :
			return new OverlayNodeSendsRegistration(din, receivedFrom);
		
		case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS :
			return new RegistryReportsRegistrationStatus(din, receivedFrom);
			
		case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION :
			return new OverlayNodeSendsDeregistration(din, receivedFrom);
		
		case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS :
			return new RegistryReportsDeregistrationStatus(din);
		
		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
			return new RegistrySendsNodeManifest(din);
		
		case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
			return new NodeReportsOverlaySetupStatus(din);
			
		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
			return new RegistryRequestsTaskInitiate(din);
			
		case Protocol.OVERLAY_NODE_SENDS_DATA:
			return new OverlayNodeSendsData(din);
			
		case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
			return new OverlayNodeReportsTaskFinished(din);
			
		case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
			return new RegistryRequestsTrafficSummary();
			
		case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
			return new OverlayNodeReportsTrafficSummary(din);
			
		default :
			LOG.debug("Unrecognized event");
			return null;
		}
	}

}