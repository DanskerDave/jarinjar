package de.davelaw.jarinjar.content;

import static de.davelaw.jarinjar.util.Util.appendSlash;
import static de.davelaw.jarinjar.util.Util.relativize;
import static de.davelaw.jarinjar.util.Util.unBackSlash;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.davelaw.jarinjar.function.TriConsumer;
import de.davelaw.jarinjar.spec.JarSpecFactory.JarFolder;
import de.davelaw.jarinjar.spec.Root;
import de.davelaw.jarinjar.zip.Zipper;

public class ResourcesContent extends Content {

	private static final Logger            LOG               = LoggerFactory.getLogger(ResourcesContent.class);

	private        final Map<String, Path> resourcesPathMap  = new TreeMap<>();


	public ResourcesContent(final JarFolder folder, final Root resources, final String... subpath) throws IOException {
		super(folder);

		final Path     singleton             = checkForSingleton(resources, subpath);
		final int      subpathLength         = subpath.length - (singleton == null ? 0 : 1);
		final String[] subpathMinusSingleton = Arrays.copyOfRange(subpath, 0, subpathLength);

		final Root     sourceSubpath         = resources .resolve(subpathMinusSingleton);

		LOG.debug("Singleton..: {}", singleton);

		walkFileTree("Res", resources .path, sourceSubpath .path, (path2resource,  fileNameRelative, fileNameNoSuffix_NULL) -> {

			if (singleton == null
			||  singleton.equals(path2resource)) {
				LOG.debug("Compare S..: {}", singleton);
				LOG.debug("Compare p..: {}", path2resource);

				this.resourcesPathMap.put(fileNameRelative, path2resource);
			}
		});
	}

	private Path checkForSingleton(final Root resources, final String... subpath) {

		if (subpath.length == 0) {
			return null;
		}

		final Root              possibleSingleton = resources.resolve(subpath);

		if (Files.isRegularFile(possibleSingleton.path)) { // Yes, its a Singleton
			return              possibleSingleton.path;
		} else {
			return null;
		}
	}

	private static void walkFileTree(final String logPrefix, final Path root, final Path subpath, final TriConsumer<Path, String, String> consumer) throws IOException {
		try {
			Files.walkFileTree(subpath, new SimpleFileVisitor<Path>() {
				/*
				 * Path file = ...
				 * BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
				 */
				@Override
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {

					final String fileNameRelative         = unBackSlash(relativize(root, path));

					consumer.accept(path, fileNameRelative, null);

					LOG.debug("{}: VISIT.: {} <- {} L={}", logPrefix, fileNameRelative, path, attrs.size());

					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (final IOException e) {
			LOG        .error("{}: VISIT.: {}",            logPrefix, root);
			throw e;
		}
	}

	@Override
	public Set<String> getClasspathEntries(final String folderName) {
		return   super.getClasspathEntries(             folderName, null);
	}

	@Override
	public void writeSources(final String folderName, final Zipper sourceZipper) {
	}

	@Override
	public void writeRuntime(final String folderName, final Zipper runtimeZipper) {

		final String folderNameSlash = appendSlash(folderName);

		this.resourcesPathMap.forEach((name, path) -> {
//			LOG.error("writeTarget: {}\t{}\t{}", folderNameSlash, name, path);
			runtimeZipper.putEntry(folderNameSlash + name, path);
		});
	}
}
