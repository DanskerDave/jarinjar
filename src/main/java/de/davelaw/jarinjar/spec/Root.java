package de.davelaw.jarinjar.spec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * A wrapper around
 * {@link  Path},
 * exposing & encapsulating necessary functionality for our purpose.
 * 
 * @author Dave
 */
public class Root {

	public         final Path path;

	public Root(final Path path) {

		if (path.isAbsolute() == false) {
			throw new NullPointerException("Path MUST be absolute.: " + path);
		}
		this.path = path;
	}

	public Root resolve(final Path subpath) {

		if (subpath.isAbsolute()) {
			throw new NullPointerException("Subpath may NOT be absolute.: " + subpath);
		}
		return new Root(this.path.resolve(subpath));
	}
	public Root resolve(final String... subpath) {

		if (subpath.length == 0) {
			return this;
		} else {
			/*
			 * As its not clearly specified in the Javadoc, what Paths.get("", subpath) returns,
			 * we split Subpath in "first" & "more" to resolve it, just to be on the safe side...
			 */
			return this.resolve(Paths.get(subpath[0], Arrays.copyOfRange(subpath, 1, subpath.length)));
		}
	}
}
