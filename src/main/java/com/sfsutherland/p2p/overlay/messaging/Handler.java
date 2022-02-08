package com.sfsutherland.p2p.overlay.messaging;

import com.sfsutherland.p2p.overlay.wireformats.Event;

public interface Handler {
	public void handle(Event e);
}
