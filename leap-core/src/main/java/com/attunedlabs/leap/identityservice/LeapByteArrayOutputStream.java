package com.attunedlabs.leap.identityservice;

import java.io.ByteArrayOutputStream;

public class LeapByteArrayOutputStream extends ByteArrayOutputStream {
	@Override
	public synchronized void reset() {
		super.count = 0;
		super.buf = new byte[super.size()];
	}
}
