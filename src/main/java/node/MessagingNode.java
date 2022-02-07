package node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import messaging.DataHandler;
import messaging.DeregistrationResponseHandler;
import messaging.Handler;
import messaging.ReceiveManifestHandler;
import messaging.RegistrationResponseHandler;
import messaging.TaskInitiateHandler;
import messaging.TrafficSummaryRequestHandler;
import util.InteractiveCommandParser;
import wireformats.Event;
import wireformats.Protocol;

public class MessagingNode extends Node {
	
	private static Logger LOG = LogManager.getLogger( MessagingNode.class);
	
	public OverlayConnectionService connectionService;

		
	public static void main(String[] args) {
		
		if (args.length < 2) {
			usageErrorExit("Please specify registry host and port number.");
		}
		int suppliedPortNumber = 0;
		String suppliedHost = args[0];
		try {
			suppliedPortNumber = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException n) {
			usageErrorExit("Please specify registry host and port number.");
		}
		
		new MessagingNode(suppliedPortNumber, suppliedHost);;

	}
	
	private MessagingNode(int suppliedPortNumber, String suppliedHost) {
		this.connectionService = new OverlayConnectionService(this, suppliedPortNumber, suppliedHost);		
		startDoingStuff();
	}
	
	private void startDoingStuff() {
		connectionService.addRegistryConnectionToConnectionsCache();			
		connectionService.startServer();
		connectionService.register();
		startCommandParser();
	}
	
	private void startCommandParser() {
		InteractiveCommandParser commandParser = new InteractiveCommandParser(this);
		Thread commandParserThread = new Thread(commandParser);
		commandParserThread.start();
	}
	

	
	@Override
	public void handleEvent(Event e) {
		
		Handler handler = null;
		switch (e.getType()) {
		
		case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
			handler = new RegistrationResponseHandler(connectionService);
			break;
		case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
			handler = new DeregistrationResponseHandler();
			break;
		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
			handler = new ReceiveManifestHandler(connectionService);
			break;
		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
			handler = new TaskInitiateHandler(connectionService);
			break;
		case Protocol.OVERLAY_NODE_SENDS_DATA:
			handler = new DataHandler(connectionService);
			break;
		case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
			handler = new TrafficSummaryRequestHandler(connectionService);
			break;
		default:
			LOG.debug("Unrecognized message received");
			break;
		}
		if (handler != null) {
			handler.handle(e);
		}
		
	}
	
	public void handleUserInput(String input) {
		switch (input) {
		
		case "exit-overlay":
			connectionService.deregister();
			break;
		case "print-counters-and-diagnostics":
			connectionService.metrics.printCountersAndDiagnostics();
			break;
		default:
			System.out.println("Unrecognized command");
			break;
		}
	}
}
