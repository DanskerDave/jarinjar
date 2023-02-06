package de.davelaw.jarinjar.content;

import static de.davelaw.jarinjar.util.Util.appendSlash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.davelaw.jarinjar.spec.JarSpecFactory.JarFolder;
import de.davelaw.jarinjar.spec.Root;
import de.davelaw.jarinjar.zip.JarInJarManifest;
import de.davelaw.jarinjar.zip.Zipper;

public class ImportedZipContent extends Content {

	private static final Logger  LOG               = LoggerFactory.getLogger(ImportedZipContent.class);

	private        final Path    jarInJarZip;


	public ImportedZipContent(final JarFolder folder, final Root jarInJarZip) throws IOException {
		super(folder);

		this.jarInJarZip   = jarInJarZip.path;
	}

	@Override
	public Set<String>   getClasspathEntries(final String folderName) {

		if (folderName.equals(JarInJarManifest.JAR_IN_JAR_FOLDER)) {
			return Collections.emptySet();
		} else {
			return super.getClasspathEntries(             folderName, null);
		}
	}

	@Override
	public void writeSources(final String folderName, final Zipper sourceZipper) {
	}

	@Override
	public void writeRuntime(final String folderName, final Zipper runtimeZipper) {

		final String folderNameSlash = appendSlash(folderName);

		try (	final InputStream    ist = Files   .newInputStream(this.jarInJarZip);
				final InputStream    bis = new BufferedInputStream(ist);
				final ZipInputStream zis = new      ZipInputStream(bis);)
		{
			ZipEntry entry;
			while ( (entry = zis.getNextEntry()) != null) {

				LOG.trace("Import Zip.: {} {} {}", entry.getSize(), folderNameSlash, entry.getName());
				/*
				 * Read in Entry bytes from Zip...
				 */
				final byte[] entryBytes  = new byte[(int) entry.getSize() + 1]; // MUST add 1, otherwise endless while-read-bytes-loop!
				/**/  int    entryOffset = 0;
				/**/  int    byteCount;

				while ((byteCount = zis.read(entryBytes, entryOffset, entryBytes.length - entryOffset)) != -1 ) {
					entryOffset += byteCount;
				}
				zis.closeEntry();
				/*
				 * Write the Entry bytes to our Runtime Zip...
				 */
				runtimeZipper.putEntry(folderNameSlash + entry.getName(), entry.getLastModifiedTime(), entryBytes);
			}
		} catch (final IOException e) {
			LOG.error("jar IN jar.: error writing Zip to Folder {}", folderName, e.getMessage());
			throw new UncheckedIOException(e);
		}
	}
}
