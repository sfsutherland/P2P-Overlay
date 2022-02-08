package com.sfsutherland.p2p.overlay.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sfsutherland.p2p.overlay.node.OverlayConnectionService;
import com.sfsutherland.p2p.overlay.routing.RoutingEntry;
import com.sfsutherland.p2p.overlay.transport.TCPConnection;
import com.sfsutherland.p2p.overlay.wireformats.Event;
import com.sfsutherland.p2p.overlay.wireformats.OverlayNodeSendsData;

public class DataHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( DataHandler.class);
	private OverlayConnectionService connectionService;

	public DataHandler(OverlayConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		OverlayNodeSendsData message = (OverlayNodeSendsData) e;
		
		// multiple receiver threads access here and mutate trackers
		if (message.destinationID != connectionService.getUniqueID()) {
			connectionService.metrics.relayTracker++;
			message.addHop(connectionService.getUniqueID());
			RoutingEntry routeEntry = connectionService.getRoutingTable().routeTo(message.destinationID);
			TCPConnection connection = connectionService.connections.getByID(routeEntry.nodeID);
			connection.send(message);
			LOG.debug("Relaying data message. relayTracker is now " + connectionService.metrics.relayTracker);
		}
			
		else {
			connectionService.metrics.receiveSummation += message.payload;
			connectionService.metrics.receiveTracker++;
			LOG.debug("Received data message meant for me, updating receiveTracker to " + connectionService.metrics.receiveTracker);
		}

	}

}
