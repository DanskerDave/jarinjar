package de.davelaw.jarinjar.content;

import static de.davelaw.jarinjar.util.Util.appendSlash;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.davelaw.jarinjar.spec.JarSpecFactory.JarFolder;
import de.davelaw.jarinjar.spec.Root;
import de.davelaw.jarinjar.zip.JarInJarManifest;
import de.davelaw.jarinjar.zip.Zipper;

public class JarLibContent extends Content {

	private static final Logger  LOG               = LoggerFactory.getLogger(JarLibContent.class);

	private        final Path    jarLib;


	public JarLibContent(final JarFolder folder, final Root jarLib) throws IOException {
		super(folder);

		this.jarLib   = jarLib.path;
	}

	@Override
	public Set<String>   getClasspathEntries(final String folderName) {

		if (folderName.equals(JarInJarManifest.JAR_IN_JAR_FOLDER)) { // TODO sort out this mess!
			return Collections.emptySet();
		} else {
			return super.getClasspathEntries(folderName, this.jarLib.getFileName().toString());
		}
	}

	@Override
	public void writeSources(final String folderName, final Zipper sourceZipper) {
	}

	@Override
	public void writeRuntime(final String folderName, final Zipper runtimeZipper) {

		final String folderNameSlash = appendSlash(folderName);

		try {
			runtimeZipper.putEntry(
					folderNameSlash         + this.jarLib.getFileName(),
					Files.getLastModifiedTime(this.jarLib),
					Files.readAllBytes       (this.jarLib));
		}
		catch (final IOException e) {
			LOG.error("jar IN jar.: error writing to Folder {} from Jar-Lib {}", folderName, this.jarLib, e.getMessage());
			throw new UncheckedIOException(e);
		}
	}
}
