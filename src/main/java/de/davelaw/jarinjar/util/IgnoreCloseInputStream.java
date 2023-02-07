package de.davelaw.jarinjar.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This allows us to pass an InputStream, without allowing it to be closed.
 * 
 * @author Dave
 * 
 * @see #IgnoreCloseInputStream(InputStream)
 */
public class IgnoreCloseInputStream extends FilterInputStream {
	/**
	 * An
	 * {@link  InputStream}
	 * wrapper which ignores calls to
	 * {@link  #close()}.
	 * 
	 * @param in
	 */
	public IgnoreCloseInputStream(final InputStream in) {
		super(in);
	}

	/**
	 * Close will be ignored. The Caller will deal with that.
	 */
	@Override
    public void close() throws IOException {
	}
}
