package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import routing.RoutingEntry;
import routing.RoutingTable;
import transport.TCPConnection;
import transport.TCPConnectionCache;
import transport.TCPServerThread;
import util.InteractiveCommandParser;
import util.StatisticsCollectorAndDisplay;
import wireformats.Event;
import wireformats.NodeReportsOverlaySetupStatus;
import wireformats.OverlayNodeReportsTaskFinished;
import wireformats.OverlayNodeReportsTrafficSummary;
import wireformats.OverlayNodeSendsDeregistration;
import wireformats.OverlayNodeSendsRegistration;
import wireformats.Protocol;
import wireformats.RegistryReportsDeregistrationStatus;
import wireformats.RegistryReportsRegistrationStatus;
import wireformats.RegistryRequestsTaskInitiate;
import wireformats.RegistryRequestsTrafficSummary;
import wireformats.RegistrySendsNodeManifest;

public class Registry implements Node {
	
	private static Logger LOG = LogManager.getLogger( Registry.class);
	private final int port;
	private ArrayList<Integer> registeredIDs;
	private Random rand;
	private boolean serverPortBound;
	private TCPConnectionCache connections;
	private ArrayList<RoutingTable> routingTables;
	private StatisticsCollectorAndDisplay statisticsCollector;
	
	private int nodeSetupCount = 0;
	private int tasksFinished = 0;
	private int trafficReportsReceived = 0;
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
			usageErrorExit();
		}
		int suppliedPortNumber = 0;
		try {
			suppliedPortNumber = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException n) {
			usageErrorExit();
		}
		new Registry(suppliedPortNumber);
	}
	
	private static void usageErrorExit() {
		System.out.println("Please specify port number.");
		System.exit(1);
	}
	
	private Registry(int port) {
		this.port = port;
		LOG.debug("New Registry initialized with port " + port);
		registeredIDs = new ArrayList<Integer>();
		this.connections = new TCPConnectionCache(this);
		this.rand = new Random();
		this.routingTables = new ArrayList<RoutingTable>();
		this.statisticsCollector = new StatisticsCollectorAndDisplay();
		
		createServerThread(connections);
		startCommandParser();
	}
	
	private void startCommandParser() {
		InteractiveCommandParser commandParser = new InteractiveCommandParser(this);
		Thread commandParserThread = new Thread(commandParser);
		commandParserThread.start();
	}
	
	private void createServerThread(TCPConnectionCache cc) {
		TCPServerThread server = new TCPServerThread(this.port, cc);
		Thread serverThread = new Thread(server);
		serverThread.start();
		waitForPortBind();
		LOG.debug("Server thread initialized and port bound");
		System.out.println("Waiting for nodes to connect");
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
	
	public int createNewID() {
		if (this.registeredIDs.size() > 127) {
			LOG.debug("Reached size limit of registered nodes");
			return -1;
		}
		Integer id;
		do {
			id = rand.nextInt(128);
		} while (this.registeredIDs.contains(id));
		this.registeredIDs.add(id);
		return id;
	}

	@Override
	public void onEvent(Event e) {
		switch(e.getType()) {
		case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
			attemptToRegister((OverlayNodeSendsRegistration) e);
			break;
		case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
			attemptToDeregister((OverlayNodeSendsDeregistration) e);
			break;
		case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
			handleNodeReportsOverlaySetupStatus((NodeReportsOverlaySetupStatus) e);
			break;
		case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
			handleOverlayNodeReportsTaskFinished((OverlayNodeReportsTaskFinished) e);
			break;
		case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
			handleOverlayNodeReportsTrafficSummary((OverlayNodeReportsTrafficSummary) e);
			break;
		default:
			LOG.debug("Unrecognized message received");
			break;
		}
	}
	
	private synchronized void handleOverlayNodeReportsTrafficSummary(OverlayNodeReportsTrafficSummary o) {
		this.statisticsCollector.addReport(o);
		LOG.debug("Received traffic summary from node " + o.id + " with receive count of " + o.totalReceived);
		if (++this.trafficReportsReceived == this.registeredIDs.size())
			notify();
	}
	
	private synchronized void handleOverlayNodeReportsTaskFinished(OverlayNodeReportsTaskFinished f) {
		this.tasksFinished++;
		LOG.debug("Node " + f.id + " has finished data transfer, " + this.tasksFinished + " tasks completed");
		if (this.tasksFinished == this.registeredIDs.size())
			notify();		

	}
	
	private void handleNodeReportsOverlaySetupStatus(NodeReportsOverlaySetupStatus n) {
		LOG.debug("Message Node responded with setup status " + n.status + " ("+ n.infoString +")");
		int status = n.status;
		if (status < 0) {
			LOG.fatal("Registration failure, exiting.");
			System.exit(1);
		}
		// receiver threads will be accessing this method concurrently
		synchronized (this) {
			if(++this.nodeSetupCount == this.registeredIDs.size())
				notify(); // so main thread can alert user that setup is complete.
		}
	}
	
	private void attemptToDeregister(OverlayNodeSendsDeregistration oe) {
		LOG.debug("attempting to deregister a node");
		String infoString;
		int status;
		Socket s = null;
		try {
			s = new Socket(oe.fromIP, oe.port);
		} catch (IOException e) {
			LOG.fatal("Creating socket for registration response");
			System.exit(1);
		}
		TCPConnection cacheConnection = connections.get(oe.port, oe.fromIP);
		if (!oe.fromIP.equals(oe.messageIPString)) {
			LOG.debug("Message IP different from connection IP");
			infoString = "Message IP different from connection IP";
			status = -1;
		}
		else if (cacheConnection == null) {
			LOG.debug("No matching connection; deregistration failed");
			infoString = "No matching connection";
			status = -1;
		}
		else if (cacheConnection.uniqueID < 0 || !this.registeredIDs.contains(cacheConnection.uniqueID)) {
			LOG.debug("Matching connection is not registered");
			infoString = "Connection matches but is not registered";
			status = -1;
		}
		else {
			LOG.debug("Deregistering id " + oe.assignedID);
			this.registeredIDs.remove(oe.assignedID);
			if (!this.connections.remove(cacheConnection)) {
				LOG.debug("Failed to remove connection from cache");
			}
			else
				LOG.debug("There are now " + this.connections.getRegisteredCount() + " registered nodes");
			status = oe.assignedID;
			infoString = "Successfully deregistered node";
		}
		Event response = new RegistryReportsDeregistrationStatus(status, infoString);
		TCPConnection responseConnection = new TCPConnection(s);
		responseConnection.send(response);
	}
	
	
	
	private void attemptToRegister(OverlayNodeSendsRegistration oe) {
		/*
		 * Check if ip and port combo in cache
		 */
		String infoString;
		int newID;
		Socket s = null;
		try {
			s = new Socket(oe.fromIP, oe.port);
		} catch (IOException e) {
			LOG.fatal("Creating socket for registration response");
			System.exit(1);
		}
		TCPConnection cacheConnection = connections.get(oe.port, oe.fromIP);
		TCPConnection responseConnection = new TCPConnection(s);
		
		if (!oe.fromIP.equals(oe.messageIPString)) {
			LOG.debug("Message IP different from connection IP");
			infoString = "Message IP different from connection IP";
			newID = -1;
		}

		else if (cacheConnection != null) {
			if (cacheConnection.uniqueID > -1) {
				LOG.debug("Already registered");
				infoString = "Already registered";
				newID = -1;
			}
			else {
				LOG.debug("Found matching connection already in cache");
				infoString = "Found matching connection already in cache";
				newID = -1;
			}
		}
		else {
			newID = createNewID();
			if (newID == -1) {
				infoString = "Reached size limit of registered nodes";
			}
			else {	
				responseConnection.uniqueID = newID;
				responseConnection.listeningPort = oe.port;
				LOG.debug("Assigned new id of " + newID + " which should match " + responseConnection.uniqueID);
				this.connections.addConnection(responseConnection);				
				infoString = "Registration request successful. The number of messaging nodes currently constituting the overlay is " + this.registeredIDs.size();
			}
		}		
		Event response = new RegistryReportsRegistrationStatus(newID, infoString);
		responseConnection.send(response);
		System.out.println("Node registered. Total system nodes: " + this.registeredIDs.size());
	}

	public void handleUserInput(String input) {
		if (input.startsWith("setup-overlay")) {
			String[] setupArgs = input.split(" ");
			int n = 3;
			if (setupArgs.length > 1) {
				try {
					n = Integer.parseInt(setupArgs[1]);
				} catch (NumberFormatException ne) {
					System.out.println("Invalid argument to setup-overlay, using default of 3");
				}
			}
			setupOverlay(n);
		}
		else if (input.startsWith("start")) {
			String[] startArgs = input.split(" ");
			if (startArgs.length < 2) {
				System.out.println("Please specify number of messages");
			}
			else {
				int n;
				try {
					n = Integer.parseInt(startArgs[1]);
				} catch (NumberFormatException ne) {
					System.out.println("Invalid argument to start");
					return;
				}
				initiateDataTransfer(n);
			}
		}
		
		else if (input.equals("list-messaging-nodes")) {
			listMessagingNodes();
		}
		
		else if (input.equals("list-routing-tables")) {
			listRoutingTables();
		}
		
		else {
			System.out.println("Unrecognized command");
		}
	}
	
	private void listRoutingTables() {

		
		for (RoutingTable table : this.routingTables) {
			System.out.println("Table for node " + table.ownerID);
			System.out.println("ID\tAddress\t\tPort");
			System.out.println("---------------------------");
			for (RoutingEntry entry : table.entryList) {
				String ip = "";
				try {
					ip = InetAddress.getByAddress(entry.address).getHostAddress();
				} catch (UnknownHostException e) {
					LOG.fatal("Unable to resolve IP from routing table entry");
				}
				System.out.println(entry.nodeID + "\t" + ip + "\t" + entry.port);
			}
			System.out.print("\n\n\n");
		}
	}
	
	private void listMessagingNodes() {
		System.out.println("ID\tHostname\t\t\tPort");
		for (int id : this.registeredIDs) {
			TCPConnection connection = this.connections.getByID(id);
			String hostname = connection.getSocket().getInetAddress().getHostName();
			System.out.println(connection.uniqueID + "\t" + hostname + "\t\t\t" + connection.listeningPort);
		}
	}
	
	private void initiateDataTransfer(int n) {
		LOG.debug("Starting data transfer with " + n + " messages");
		Event startMessage = new RegistryRequestsTaskInitiate(n);
		for (TCPConnection conn : this.connections.connectionList) {
			conn.send(startMessage);
		}
		synchronized (this) {
			if (this.tasksFinished != this.registeredIDs.size()) {
				try {
					wait();
				} catch (InterruptedException e) {
					LOG.debug("Interrupted while waiting for tasks to finish");
				}
			}
			LOG.debug(this.tasksFinished + " finished tasks");
			this.tasksFinished = 0; // so we can restart
			requestTrafficSummaries();
		}
	}
	
	private void requestTrafficSummaries() {
		Event trafficRequest = new RegistryRequestsTrafficSummary();
		for (TCPConnection conn : this.connections.connectionList)
			conn.send(trafficRequest);
		
		// wait here until all reports are in
		synchronized (this) {
			if (this.trafficReportsReceived != this.registeredIDs.size()) {
				try {
					wait();
				} catch (InterruptedException e) {
					LOG.debug("Interrupted while waiting for traffic reports");
				}
			}
			LOG.debug("All traffic reports in");
			this.trafficReportsReceived = 0; // restartability
			this.statisticsCollector.showStatistics();
		}

	}
	
	private void setupOverlay(int n) {
		LOG.debug("Setting up overlay with " + n + " entries per routing table");
		Collections.sort(this.registeredIDs);
		
		for (int i = 0; i < this.registeredIDs.size(); i++ ) {
			RoutingTable table = new RoutingTable(this.registeredIDs.get(i));
			
			for (int j = 0; j < n; j++) {
				RoutingEntry entry = createRoutingEntry(i,j);
				table.addEntry(entry);
			}
			this.routingTables.add(table);
			Event event = new RegistrySendsNodeManifest(table, this.registeredIDs); // and registeredIDs list
			TCPConnection tableConnection = this.connections.getByID(this.registeredIDs.get(i));
			tableConnection.send(event);
		}
		
		// wait for all nodes to respond successfully.
		// always called before notify happens. No need to test this.nodeSetupCount...?
		LOG.debug("Waiting until nodes respond...");
		
		synchronized (this) {
			if (this.nodeSetupCount != this.registeredIDs.size()) {
				try {
					wait();
				} catch (InterruptedException e) {
					LOG.debug("Interrupted while waiting on setup-overlay", e);
				}
				this.nodeSetupCount = 0; // NOT SUFFICIENT CONDITION TO RESTART SETUP
				System.out.println("Registry now ready to initiate tasks");
			}
		}
		
	}
	
	private RoutingEntry createRoutingEntry(int currentIndex, int power) {
		int hops = (int) Math.pow(2, power);
		int neighborIndex = (currentIndex + hops) % this.registeredIDs.size();
		if (neighborIndex == currentIndex) {
			LOG.fatal("Node matches self at " + hops + " hops");
			System.exit(1);
		}
		int nodeID = this.registeredIDs.get(neighborIndex);
		TCPConnection entryConnection = this.connections.getByID(nodeID);
		if (entryConnection == null) {
			LOG.fatal("Connection with id " + nodeID + " has disappeared from cache");
			System.exit(1);
		}
		RoutingEntry entry = new RoutingEntry(nodeID, entryConnection.remoteIP, entryConnection.listeningPort);
		return entry;
	}

}
