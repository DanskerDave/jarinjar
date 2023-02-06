 package de.davelaw.jarinjar.zip;

import java.nio.file.attribute.FileTime;

public final class ByteArrayEntry {

	public final String   entryName;
	public final FileTime entryTime;
	public final byte[]   entryBytes;

	public ByteArrayEntry(final CharSequence entryName, final FileTime entryTime, final byte[] entryBytes) {

		this.entryName  = entryName.toString();
		this.entryTime  = entryTime;
		this.entryBytes = entryBytes;
	}
}
