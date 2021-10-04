package wireformats;

public interface Protocol {
	public static final byte OVERLAY_NODE_SENDS_REGISTRATION = 2;
	public static final byte REGISTRY_REPORTS_REGISTRATION_STATUS = 3;

	public static final byte OVERLAY_NODE_SENDS_DEREGISTRATION = 4;
	public static final byte REGISTRY_REPORTS_DEREGISTRATION_STATUS = 5;

	public static final byte REGISTRY_SENDS_NODE_MANIFEST = 6;
	public static final byte NODE_REPORTS_OVERLAY_SETUP_STATUS = 7;

	public static final byte REGISTRY_REQUESTS_TASK_INITIATE = 8;
	public static final byte OVERLAY_NODE_SENDS_DATA = 9;
	public static final byte OVERLAY_NODE_REPORTS_TASK_FINISHED = 10;

	public static final byte REGISTRY_REQUESTS_TRAFFIC_SUMMARY = 11;
	public static final byte OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY = 12;
	

	public static String typeString(byte b) {
		switch(b) {
		
		case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
			return "OVERLAY_NODE_SENDS_REGISTRATION";
			
		case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
			return "REGISTRY_REPORTS_REGISTRATION_STATUS";
		
		case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
			return "OVERLAY_NODE_SENDS_DEREGISTRATION";
		
		case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
			return "REGISTRY_REPORTS_DEREGISTRATION_STATUS";
		
		case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
			return "REGISTRY_SENDS_NODE_MANIFEST";		
		
		case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
			return "NODE_REPORTS_OVERLAY_SETUP_STATUS";			
		
		case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
			return "REGISTRY_REQUESTS_TASK_INITIATE";			
		
		case Protocol.OVERLAY_NODE_SENDS_DATA:
			return "OVERLAY_NODE_SENDS_DATA";			
		
		case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
			return "OVERLAY_NODE_REPORTS_TASK_FINISHED";
			
		case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
			return "REGISTRY_REQUESTS_TRAFFIC_SUMMARY";
			
		case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
			return "OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY";
			
		default:
			return "unrecognized";
		}
	}
}