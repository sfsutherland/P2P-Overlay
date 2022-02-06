package node;

public class MetricsData {

	// TODO synchronize update of these trackers by multiple threads!
	public long sendSummation = 0;
	public long receiveSummation = 0;
	public int sendTracker = 0;
	public int receiveTracker = 0;
	public int relayTracker = 0;
		
		
	public void printCountersAndDiagnostics() {
		System.out.println("|ID\t\t|Packets Sent\t|Packets Received\t|Packets Relayed\t|Sum Values Sent\t|Sum Values Received|\n");
		System.out.println("| Totals\t|" + this.sendTracker +"\t\t|" +this.receiveTracker+ "\t\t\t|"+ this.relayTracker+ "\t\t\t|" +this.sendSummation+ "\t|" +this.receiveSummation + "|");
	}

	
}
