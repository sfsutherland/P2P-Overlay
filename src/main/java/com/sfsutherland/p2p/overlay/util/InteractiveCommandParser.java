package com.sfsutherland.p2p.overlay.util;

import java.util.Scanner;

import com.sfsutherland.p2p.overlay.node.Node;

public class InteractiveCommandParser implements Runnable{
	
	private Node node;
	private Scanner scanner;

	public InteractiveCommandParser(Node n) {
		this.node = n;
		this.scanner = new Scanner(System.in);
	}
	@Override
	public void run() {
		while(true) {
			System.out.print("> ");
			String input = scanner.nextLine();
			node.handleUserInput(input);
		}
		
	}
}
