package node;

import java.io.IOException;
import java.net.InetAddress;
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
import util.InteractiveCommandParser;
import wireformats.Event;
import wireformats.NodeReportsOverlaySetupStatus;
import wireformats.OverlayNodeReportsTaskFinished;
import wireformats.OverlayNodeReportsTrafficSummary;
import wireformats.OverlayNodeSendsData;
import wireformats.OverlayNodeSendsDeregistration;
import wireformats.OverlayNodeSendsRegistration;
import wireformats.Protocol;
import wireformats.RegistryReportsDeregistrationStatus;
import wireformats.RegistryReportsRegistrationStatus;
import wireformats.RegistryRequestsTaskInitiate;
import wireformats.RegistrySendsNodeManifest;

public class MessagingNode implements Node {
	
	private static Logger LOG = LogManager.getLogger( MessagingNode.class);
	private final int registryPort;
	private final String registryHost;
	private boolean serverPortBound = false;
	private int listeningPort;
	private int uniqueID;
	private TCPConnectionCache connections;
	private ArrayList<Integer> systemIDs = null;
	private Random random;
	private RoutingTable routingTable = null;
	private long sendSummation = 0;
	private long receiveSummation = 0;
	private int sendTracker = 0;
	private int receiveTracker = 0;
	private int relayTracker = 0;
	
	private static void usageErrorExit() {
		System.out.println("Please specify registry host and port number.");
		System.exit(1);
	}
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			usageErrorExit();
		}
		int suppliedPortNumber = 0;
		String suppliedHost = args[0];
		try {
			suppliedPortNumber = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException n) {
			usageErrorExit();
		}
		
		new MessagingNode(suppliedHost, suppliedPortNumber);

	}
	public synchronized void setPortBound(boolean b) {
		this.serverPortBound = b;
		if (serverPortBound) notify();
	}
	
	private synchronized void waitForPortBind() {
		if (!serverPortBound)
			try {
				wait();
			} catch (InterruptedException e) {
				LOG.debug("wait() interrupted");
			}
	}
	
	private MessagingNode(String host, int port) {
		this.registryPort = port;
		this.registryHost = host;
		this.random = new Random();
		
		LOG.debug("New MessagingNode initialized for registry "
				+ this.registryHost + " on port " + this.registryPort);
		Socket socket = null;
		try {
			socket = new Socket(registryHost, registryPort);
		} catch (IOException e) {
			LOG.fatal("Initializing socket");
			System.exit(1);
		}
		this.connections = new TCPConnectionCache(this);
		TCPConnection registryConnection = new TCPConnection(socket);
		this.connections.addRegistry(registryConnection);
				
		createServerThread(connections);
		register(registryConnection);
		startCommandParser();
	}
	
	private void startCommandParser() {
		InteractiveCommandParser commandParser = new InteractiveCommandParser(this);
		Thread commandParserThread = new Thread(commandParser);
		commandParserThread.start();
	}
	
	private void createServerThread(TCPConnectionCache cc) {
		TCPServerThread server = new TCPServerThread(cc);
		Thread serverThread = new Thread(server);
		serverThread.start();
		waitForPortBind(); // server thread has notified us that a port is bound
		this.listeningPort = server.port;
		LOG.debug("Listening on port " + this.listeningPort);
	}
	
	private void register(TCPConnection registryConnection) {
		Event event = new OverlayNodeSendsRegistration(this.listeningPort);
		registryConnection.send(event);
	}

	@Override
	public void onEvent(Event e) {
		switch (e.getType()) {
		
		case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
			handleRegistrationResponse((RegistryReportsRegistrationStatus)e);
			break;
		case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
			handleDeregistrationResponse((RegistryReportsDeregistrationStatus)e);
			break;
		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
			receiveNodeManifest((RegistrySendsNodeManifest) e);
			break;
		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
			handleRegistryRequestsTaskInitiate((RegistryRequestsTaskInitiate)e);
			break;
		case Protocol.OVERLAY_NODE_SENDS_DATA:
			handleOverlayNodeSendsData((OverlayNodeSendsData)e);
			break;
		case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
			handleRegistryRequestsTrafficSummary();
			break;
		default:
			LOG.debug("Unrecognized message received");
			break;
		}
	}
	
	private void handleRegistryRequestsTrafficSummary() {
		Event trafficReport = new OverlayNodeReportsTrafficSummary(this.uniqueID, this.sendTracker, this.relayTracker, this.sendSummation, this.receiveTracker, this.receiveSummation);
		this.connections.getRegistry().send(trafficReport);
	}
	
	private synchronized void handleOverlayNodeSendsData(OverlayNodeSendsData d) {
		// multiple receiver threads access here and mutate trackers
		if (d.destinationID != this.uniqueID) {
			this.relayTracker++;
			relayMessage(d);
			LOG.debug("Relaying data message. relayTracker is now " + this.relayTracker);
		}
			
		else {
			this.receiveSummation += d.payload;
			this.receiveTracker++;
			LOG.debug("Received data message meant for me, updating receiveTracker to " + this.receiveTracker);
		}
	}
	
	private void relayMessage(OverlayNodeSendsData d) {
		d.addHop(this.uniqueID);
		RoutingEntry routeEntry = this.routingTable.routeTo(d.destinationID);
		TCPConnection connection = this.connections.getByID(routeEntry.nodeID);
		connection.send(d);
	}
	
	private void handleRegistryRequestsTaskInitiate(RegistryRequestsTaskInitiate r) {
		System.out.println("Starting message transfer with " + r.numberOfMessages + " messages");

		for (int i = 0; i < r.numberOfMessages; i++) {
			sendDataMessage();
		}
		// send task_finished_message
		Event finishedMessage = new OverlayNodeReportsTaskFinished(this.listeningPort, this.uniqueID);
		this.connections.getRegistry().send(finishedMessage);
		System.out.println("Finished sending messages");
	}
	
	private void sendDataMessage() {
		int index = this.random.nextInt(this.systemIDs.size());
		LOG.debug("Random index is " + index);
		
		int recipientNode = this.systemIDs.get(index);
		LOG.debug("destination ID is " + recipientNode);
		
		RoutingEntry routeEntry = this.routingTable.routeTo(recipientNode);
		int payload = this.random.nextInt();
		Event message = new OverlayNodeSendsData(recipientNode, this.uniqueID, payload);
		
		// send message to routeEntry
		LOG.debug("Sending data message to node " + recipientNode + " via node " + routeEntry.nodeID);
		TCPConnection connection = this.connections.getByID(routeEntry.nodeID);
		if (connection == null) {
			LOG.fatal("Unable to find connection to node " + routeEntry.nodeID);
			System.exit(1);
		}
		connection.send(message);
		
		// Only accessed by single thread
		this.sendSummation += payload;
		this.sendTracker++;
	}
	
	private void receiveNodeManifest(RegistrySendsNodeManifest r) {
		int status  = this.uniqueID;
		String infoString;
		
		this.routingTable = r.routingTable;
		LOG.debug("Received Node manifest with " + this.routingTable.getSize() + " entries");
		for (RoutingEntry entry : this.routingTable.entryList) {
			Socket socket = null;
			try {
				socket = new Socket(InetAddress.getByAddress(entry.address), entry.port);
			} catch (IOException e) {
				LOG.debug("Failed to initialize socket to neighbor");
				infoString = "Failed to initialize socket to neighbor";
				status = -1;
				break;
			}			
			TCPConnection neighborConnection = new TCPConnection(socket);
			neighborConnection.listeningPort = entry.port;
			neighborConnection.uniqueID = entry.nodeID;			
			this.connections.addConnection(neighborConnection);
			LOG.debug("Added neighbor node connection to cache with id " + entry.nodeID);
			
		}
		this.systemIDs = r.allSystemNodes;
		
		this.systemIDs.remove(this.uniqueID);
		
		infoString = "Successfully connected to " + this.routingTable.getSize() + " neighbors";
		Event responseEvent = new NodeReportsOverlaySetupStatus(status, infoString);
		this.connections.getRegistry().send(responseEvent);
	}
	
	private void handleDeregistrationResponse(RegistryReportsDeregistrationStatus r) {
		LOG.debug("Received " + Protocol.typeString(r.getType()) + " from registry");
		LOG.debug("Deregistration status: " + r.status + "(" + r.infoString + ")");
		System.exit(0);
	}
	
	private void handleRegistrationResponse(RegistryReportsRegistrationStatus r) {
		int id = r.assignedID;
		LOG.debug("Received registration ID of " + id + " and message string: " + r.infoString);
		if (id == -1) {
			LOG.fatal("Registration failed");
			System.exit(1);
		}
		else this.uniqueID = id;
		System.out.println("Successfully registered");
	}
	
	public void handleUserInput(String input) {
		switch (input) {
		
		case "exit-overlay":
			deregister();
			break;
		case "print-counters-and-diagnostics":
			printCountersAndDiagnostics();
			break;
		default:
			System.out.println("Unrecognized command");
			break;
		}
	}
	
	private void printCountersAndDiagnostics() {
		System.out.println("|ID\t\t|Packets Sent\t|Packets Received\t|Packets Relayed\t|Sum Values Sent\t|Sum Values Received|\n");
		System.out.println("| Totals\t|" + this.sendTracker +"\t\t|" +this.receiveTracker+ "\t\t\t|"+ this.relayTracker+ "\t\t\t|" +this.sendSummation+ "\t|" +this.receiveSummation + "|");
	}
	
	private void deregister() {
		Event event = new OverlayNodeSendsDeregistration(this.listeningPort, this.uniqueID);
		LOG.debug("Sending " + Protocol.typeString(event.getType()));
		this.connections.getRegistry().send(event);
	}

}
