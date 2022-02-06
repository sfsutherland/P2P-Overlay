package node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import routing.RoutingEntry;
import routing.RoutingTable;
import transport.TCPConnection;
import transport.TCPConnectionCache;
import transport.TCPServerThread;
import wireformats.Event;
import wireformats.OverlayNodeSendsData;
import wireformats.OverlayNodeSendsRegistration;
import wireformats.Protocol;

public class ConnectionService {
	
	private static Logger LOG = LogManager.getLogger( ConnectionService.class);
		
	private Node node;
	private final int registryPort;
	private final String registryHost;
	private Random random;
	private RoutingTable routingTable = null;
	private int uniqueID;
	
	public int listeningPort;
	public MetricsData metrics;
	public ArrayList<Integer> systemIDs = null;
	public TCPConnectionCache connections;


	

	public ConnectionService(Node node, int registryPort, String registryHost) {
		this.node = node;
		this.registryPort = registryPort;
		this.registryHost = registryHost;
		this.connections = new TCPConnectionCache(this.node);
		
		
		LOG.debug("New Connection Service initialized for registry "
				+ this.registryHost + " on port " + this.registryPort);
	}
	
	public void addRegistryConnectionToConnectionsCache() {
		this.connections.addRegistry(createTCPConnectionToRegistry());
	}
	
	private TCPConnection createTCPConnectionToRegistry() {
		Socket socket = null;
		try {
			socket = new Socket(registryHost, registryPort);
		} catch (IOException e) {
			LOG.fatal("Initializing socket to registry");
			System.exit(1);
		}
		
		return new TCPConnection(socket);		
	}
	
	
	public void startServer() {
		TCPServerThread server = startServerThread();
		rememberServerListeningPort(server);
	}
	
	private TCPServerThread startServerThread() {
		TCPServerThread server = new TCPServerThread(this.connections);
		Thread serverThread = new Thread(server);
		serverThread.start();
		return server;
	}	
	
	private void rememberServerListeningPort(TCPServerThread server) {
		waitForPortBind(); // server thread has notified us that a port is bound
		this.listeningPort = server.port;
		LOG.debug("Listening on port " + this.listeningPort);		
	}
	
	public void register() {
		Event registrationRequestEvent = new OverlayNodeSendsRegistration(this.listeningPort);
		sendEventToRegistry(registrationRequestEvent);
	}
	
	public void sendEventToRegistry(Event event) {
		LOG.debug("Sending " + Protocol.typeString( event.getType() ) + " event to registry.");
		this.connections.getRegistry().send(event);
	}
	
	public void sendEventToOverlayNode(int recipientID, Event event) {
		
		RoutingEntry routeEntry = this.routingTable.routeTo(recipientID);
				
		TCPConnection connection = this.connections.getByID(routeEntry.nodeID);
		
		if (connection == null) {
			LOG.fatal("Unable to find connection to node " + routeEntry.nodeID);
			System.exit(1);
		}
		
		LOG.debug("Sending data message to node " + recipientID + " via node " + routeEntry.nodeID);
		connection.send(event);
		
		metrics.sendTracker++;
	}
	
	public void sendRandomDataMessage() {
		
		int randomRecipientID = getRandomNodeID();
				
		Event dataMessage = this.createRandomDataEventMessage(randomRecipientID);
		
		sendEventToOverlayNode(randomRecipientID, dataMessage);

	}
	
	private int getRandomNodeID() {
		int index = this.random.nextInt(this.systemIDs.size());
		
		int randomRecipientNodeID = this.systemIDs.get(index);
		LOG.debug("destination ID is " + randomRecipientNodeID);
		
		return randomRecipientNodeID;
	}
	
	private Event createRandomDataEventMessage(int recipientID) {
		int payload = this.random.nextInt();
		metrics.sendSummation += payload;
		return new OverlayNodeSendsData(recipientID, this.uniqueID, payload);
	}
	public int getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(int uniqueID) {
		this.uniqueID = uniqueID;
	}
	
	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public void setRoutingTable(RoutingTable routingTable) {
		this.routingTable = routingTable;
	}
}
