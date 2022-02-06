package messaging;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.OverlayConnectionService;
import routing.RoutingEntry;
import transport.TCPConnection;
import wireformats.Event;
import wireformats.NodeReportsOverlaySetupStatus;
import wireformats.RegistrySendsNodeManifest;

public class ReceiveManifestHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( ReceiveManifestHandler.class);
	private OverlayConnectionService connectionService;
	
	public ReceiveManifestHandler(OverlayConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		
		// TODO handle casting errors by throwing something
		RegistrySendsNodeManifest message = (RegistrySendsNodeManifest) e;
		
		int status  = connectionService.getUniqueID();
		String infoString;
		
		connectionService.setRoutingTable(message.routingTable);
		
		LOG.debug("Received Node manifest with " + message.routingTable.getSize() + " entries");
		for (RoutingEntry entry : message.routingTable.entryList) {
			Socket socket = null;
			try {
				socket = new Socket(InetAddress.getByAddress(entry.address), entry.port);
			} catch (IOException ex) {
				LOG.debug("Failed to initialize socket to neighbor");
				infoString = "Failed to initialize socket to neighbor";
				status = -1;
				break;
			}			
			TCPConnection neighborConnection = new TCPConnection(socket);
			neighborConnection.listeningPort = entry.port;
			neighborConnection.uniqueID = entry.nodeID;			
			connectionService.connections.addConnection(neighborConnection);
			LOG.debug("Added neighbor node connection to cache with id " + entry.nodeID);
			
		}
		connectionService.systemIDs = message.allSystemNodes;
		
		connectionService.systemIDs.remove(connectionService.getUniqueID());
		
		infoString = "Successfully connected to " + message.routingTable.getSize() + " neighbors";
		Event responseEvent = new NodeReportsOverlaySetupStatus(status, infoString);
		connectionService.connections.getRegistry().send(responseEvent);

	}

}
