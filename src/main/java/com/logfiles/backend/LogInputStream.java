package com.logfiles.backend;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract class to save the reference on where the cursor is after reading.
 * @author alexdel
 *
 */
public abstract class LogInputStream extends InputStream {
	@Override
	abstract public int read() throws IOException;
	abstract public long getLastPos();
	abstract protected void allocateAndCacheBuffer(long start, long end) throws IOException;
}
