package routing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoutingEntry {

	public final int nodeID;
	public final int port;
	public byte[] address = null;
	private static Logger LOG = LogManager.getLogger(RoutingEntry.class);
	
	public RoutingEntry(int id, String ip, int port) {
		this.nodeID = id;
		try {
			this.address = InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			LOG.fatal("Could not extract ip from string");
			System.exit(1);
		}
		this.port = port;
	}
	
	public RoutingEntry(int id, byte[] ip, int port) {
		this.nodeID = id;
		this.address = ip;
		this.port = port;
	}
}
