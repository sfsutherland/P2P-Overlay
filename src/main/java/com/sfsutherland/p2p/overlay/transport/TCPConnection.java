package com.sfsutherland.p2p.overlay.transport;

import java.net.Socket;

import com.sfsutherland.p2p.overlay.wireformats.Event;

public class TCPConnection {

	public int uniqueID;
	private Socket socket;
	public int listeningPort = -1;
	public final String remoteIP;
	private TCPSender sender;
	
	public TCPConnection(Socket s) {
		this.socket = s;
		this.remoteIP = s.getInetAddress().getHostAddress();
		this.sender = new TCPSender(s);
		this.uniqueID = -1;
	}
	
	public void send(Event event) {
		sender.sendMessage(event);
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
}
