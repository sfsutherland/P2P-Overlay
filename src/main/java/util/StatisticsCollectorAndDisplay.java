package util;

import java.util.ArrayList;

import wireformats.OverlayNodeReportsTrafficSummary;

public class StatisticsCollectorAndDisplay {

	private ArrayList<OverlayNodeReportsTrafficSummary> nodeReports;
	
	public StatisticsCollectorAndDisplay() {
		this.nodeReports = new ArrayList<OverlayNodeReportsTrafficSummary>();
	}
	
	public synchronized void addReport(OverlayNodeReportsTrafficSummary report) {
		this.nodeReports.add(report);
	}
	
	public void showStatistics() {

		int totalSent = 0;
		int totalRelayed = 0;
		long sentSummation = 0;
		int totalReceived = 0;
		long receivedSummation = 0;
		System.out.println("|ID\t\t|Packets Sent\t|Packets Received\t|Packets Relayed\t|Sum Values Sent\t|Sum Values Received|\n");
		for (OverlayNodeReportsTrafficSummary report : this.nodeReports) {
			System.out.println("| " +report.id + "\t\t|" + report.totalSent+ "\t\t|" +report.totalReceived+ "\t\t\t|" +report.totalRelayed+ "\t\t\t|" +report.sentSummation+ "\t|" +report.receivedSummation + "|");
			totalSent += report.totalSent;
			totalRelayed += report.totalRelayed;
			sentSummation += report.sentSummation;
			totalReceived += report.totalReceived;
			receivedSummation += report.receivedSummation;
		}
		System.out.println("| Totals\t|" + totalSent+ "\t\t|" +totalReceived+ "\t\t\t|"+ totalRelayed+ "\t\t\t|" +sentSummation+ "\t|" +receivedSummation + "|");
	}
}
