package transport;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import node.Node;

public class TCPConnectionCache {
	
	private static Logger LOG = LogManager.getLogger( TCPConnectionCache.class);
	public Node node;
	private TCPConnection registry;
	public ArrayList<TCPConnection> connectionList;
	
	public TCPConnectionCache(Node n) {
		this.node = n;
		connectionList = new ArrayList<TCPConnection>();
	}
	
	public void addRegistry(TCPConnection c) {
		this.registry = c;
	}
	
	public synchronized void addConnection(TCPConnection c) {
		connectionList.add(c);
		LOG.debug("Added connection (" + c.remoteIP + ", " + c.listeningPort +") to cache. There are now " + connectionList.size() + " total items.");
	}
	
	public TCPConnection getRegistry() {
		return this.registry;
	}
	
	public int getRegisteredCount() {
		return this.connectionList.size();
	}
	
	public synchronized boolean contains(TCPConnection c) {
		for (TCPConnection cd: connectionList) {
			if ((cd.remoteIP.equals(c.remoteIP)) && (cd.listeningPort == c.listeningPort))
				return true;
		}
		return false;
	}
	
	public synchronized TCPConnection getByID(int id) {
		//LOG.debug("Retrieving connection with id " + id + " from cache");
		for (TCPConnection cd: connectionList) {
			if (cd.uniqueID == id) {
				//LOG.debug("Found connection!");
				return cd;
			}
		}
		return null;
	}
	
	public synchronized TCPConnection get(int port, String ip) {
		LOG.debug("Searching for connection to " + ip + " on port " + port);
		int matchCount = 0;
		TCPConnection found = null;
		
		for (TCPConnection cd: connectionList) {
			if ((cd.remoteIP.equals(ip)) && (cd.listeningPort == port)) {
				found = cd;
				matchCount++;
			}
		}
		if (matchCount > 1) {
			LOG.fatal("Duplicate connections matched!");
			System.exit(1);
		}
		return found;
	}
	public synchronized boolean remove(TCPConnection c) {
		return connectionList.remove(c);
	}

}
