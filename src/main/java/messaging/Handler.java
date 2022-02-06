package messaging;

import wireformats.Event;

public interface Handler {
	public void handle(Event e);
}
