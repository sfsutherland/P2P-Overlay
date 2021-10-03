package node;

import wireformats.Event;

public interface Node {
	public void onEvent(Event e);
	public void setPortBound(boolean b);
	public void handleUserInput(String s);
}