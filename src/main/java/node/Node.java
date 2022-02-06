package node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import transport.TCPConnectionCache;
import wireformats.Event;

public abstract class Node {
	
	private static Logger LOG = LogManager.getLogger( Node.class);
	private boolean serverPortBound = false;
	protected TCPConnectionCache connections;
	
	public abstract void onEvent(Event e);

	public abstract void handleUserInput(String s);
	
	static void usageErrorExit(String s) {
		System.out.println("ERROR: " + s);
		System.exit(0);
	}
	
	public synchronized void setPortBound() {
		this.serverPortBound = true;
		notify();
	}
	
	protected synchronized void waitForPortBind() {
		if (!serverPortBound)
			try {
				wait();
			} catch (InterruptedException e) {
				LOG.debug("wait() interrupted");
			}
	}
}