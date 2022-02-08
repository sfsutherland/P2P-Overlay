package com.sfsutherland.p2p.overlay.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.node.OverlayConnectionService;
import com.sfsutherland.p2p.overlay.wireformats.Event;
import com.sfsutherland.p2p.overlay.wireformats.OverlayNodeReportsTrafficSummary;

public class TrafficSummaryRequestHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( TrafficSummaryRequestHandler.class);
	private OverlayConnectionService connectionService;

	public TrafficSummaryRequestHandler(OverlayConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		OverlayNodeReportsTrafficSummary message = (OverlayNodeReportsTrafficSummary) e;
		
		Event trafficReport = new OverlayNodeReportsTrafficSummary(connectionService.getUniqueID(), connectionService.metrics);
		connectionService.sendEventToRegistry(trafficReport);

	}

}
