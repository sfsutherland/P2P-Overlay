package com.sfsutherland.p2p.overlay.wireformats;

import java.io.IOException;

public interface Event {
	public byte[] getBytes() throws IOException;
	public byte getType();
}
