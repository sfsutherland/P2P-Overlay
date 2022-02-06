package messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.ConnectionService;
import wireformats.Event;
import wireformats.OverlayNodeReportsTaskFinished;
import wireformats.RegistryRequestsTaskInitiate;

public class TaskInitiateHandler implements Handler {

	private static Logger LOG = LogManager.getLogger( TaskInitiateHandler.class);
	private ConnectionService connectionService;

	public TaskInitiateHandler(ConnectionService cs) {
		this.connectionService = cs;
	}
	@Override
	public void handle(Event e) {
		RegistryRequestsTaskInitiate message = (RegistryRequestsTaskInitiate) e;
		System.out.println("Starting message transfer with " + message.numberOfMessages + " messages");

		for (int i = 0; i < message.numberOfMessages; i++) {
			connectionService.sendRandomDataMessage();;
		}
		reportTaskFinished();
	}
	
	private void reportTaskFinished() {
		// send task_finished_message
		Event finishedMessage = new OverlayNodeReportsTaskFinished(connectionService.listeningPort, connectionService.getUniqueID());
		connectionService.sendEventToRegistry(finishedMessage);
		
		LOG.info("Finished sending all messages");
	}
	
	
}
