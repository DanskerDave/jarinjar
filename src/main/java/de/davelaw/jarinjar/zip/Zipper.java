package de.davelaw.jarinjar.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.davelaw.jarinjar.function.ExConsumer;
import de.davelaw.jarinjar.util.Util;

public final class Zipper {

	private static final Logger                     LOG         = LoggerFactory.getLogger(Zipper.class);

	private        final ZipOutputStream            zipOutputStream;
	private        final Function<String, ZipEntry> zipEntryFactory;

	private              int                        entryCount  = 0;
	private              FileTime                   maxFileTime = FileTime.fromMillis(0);

	private Zipper(final Manifest manifest, final OutputStream outputStream, final ExConsumer<Zipper, IOException> entryFactory) throws IOException {

		LOG    .trace("new Zipper.: {} {}", manifest, outputStream);

		try (	final    OutputStream        ost =              outputStream;
				final    OutputStream        bos = new  BufferedOutputStream(ost);
				final ZipOutputStream        zos = manifest != null ?           new JarOutputStream(bos, manifest) :           new ZipOutputStream(bos) )
		{
			this.zipEntryFactory                 = manifest != null ? (name) -> new JarEntry(name)                 : (name) -> new ZipEntry(name);
			this.zipOutputStream                 = zos;

			this.zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);

			entryFactory.accept(this);

			LOG.trace("Zipper done: {} {}", manifest, outputStream);
		}
		catch (final IOException e) {
			LOG.error("new Zipper.: {} {}", manifest, outputStream, e);
			throw e;
		}
	}

	public void putEntry(final String entryName, final Path entryLocation) {
		try {
			putEntry(entryName, Files.getLastModifiedTime(entryLocation), Files.readAllBytes(entryLocation));
		}
		catch (final IOException e) {
			LOG.error("put Member.: {}\t{}", this.entryCount, entryName, e);
			throw new UncheckedIOException(e);
		}
	}

	public void putEntry(final ByteArrayEntry zipJarEntry) {
		putEntry(zipJarEntry.entryName, zipJarEntry.entryTime, zipJarEntry.entryBytes);
	}

	public void putEntry(final String entryName, final FileTime entryTime, final byte[] entryBytes) {
		putEntry        (             entryName,                entryTime,              entryBytes,               0, entryBytes.length);
	}

	public void putEntry(final String entryName, final FileTime entryTime, final byte[] entryBytes, final int offset, final int length) {

		putEntry        (             entryName,                entryTime, new ByteArrayInputStream(entryBytes, offset, length), length);
	}

	/**
	 * 
	 * @param entryName
	 * @param entryTime
	 * @param inputStream      the Caller is responsible for closing this Stream
	 * @param estimatedLength  some Zips, particularly under JDK 8, return -1 as the length
	 */
	public void putEntry(final String entryName, final FileTime entryTime, final InputStream inputStream, final long estimatedLength) {
		try {
			this.entryCount++;
			this.updateMaxFileTime(entryTime);

			final ZipEntry                    entry = this.zipEntryFactory.apply(entryName);
			/**/                              entry.setLastModifiedTime         (entryTime);

			this.zipOutputStream.putNextEntry(entry);

			final long actualLength = Util.copyBytes(inputStream/* (closed by caller) */, this.zipOutputStream/* (closed in our Constructor) */);

			this.zipOutputStream.closeEntry();

			LOG.debug("put Member.: {}\t{}\tL={}\t{}", this.entryCount, entryTime,    actualLength, entryName);
		}
		catch (final IOException e) {
			LOG.error("put Member.: {}\t{}\tL={}\t{}", this.entryCount, entryTime, estimatedLength, entryName, e);
			throw new UncheckedIOException(e);
		}
	}

	public int getEntryCount() {
		return this.entryCount;
	}

	public FileTime getMaxFileTime() {
		return this.maxFileTime;
	}

	private void updateMaxFileTime(final FileTime entryTime) {

		if (this.maxFileTime.compareTo(entryTime) < 0) {
			this.maxFileTime =         entryTime;
		}
	}

	public static  Zipper createZip(                         final OutputStream outputStream, final ExConsumer<Zipper, IOException> entryFactory) throws IOException {
		return new Zipper          (                   null,                    outputStream,                                       entryFactory);
	}
	public static  Zipper createJar(final Manifest manifest, final OutputStream outputStream, final ExConsumer<Zipper, IOException> entryFactory) throws IOException {
		return new Zipper          (               manifest,                    outputStream,                                       entryFactory);
	}
}
