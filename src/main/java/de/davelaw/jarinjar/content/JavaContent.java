package de.davelaw.jarinjar.content;

import static de.davelaw.jarinjar.util.Util.appendSlash;
import static de.davelaw.jarinjar.util.Util.relativize;
import static de.davelaw.jarinjar.util.Util.removeFromChar;
import static de.davelaw.jarinjar.util.Util.removeSuffix;
import static de.davelaw.jarinjar.util.Util.unBackSlash;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class JavaContent extends Content {

	private static final Logger            LOG               = LoggerFactory.getLogger(JavaContent.class);

	private static final Path              DEFAULT_SOURCE    = Paths.get("src",    "main",    "java");
	public  static final Path              DEFAULT_RESOURCES = Paths.get("src",    "main",    "resources");
	private static final Path              DEFAULT_RUNTIME   = Paths.get("target", "classes");

	private static final String            DOT_JAVA          = ".java";
	private static final String            DOT_CLASS         = ".class";

	private        final Map<String, Path> sourcesPathMap    = new TreeMap<>();
	private        final Map<String, Path> runtimePathMap    = new TreeMap<>();


	public JavaContent(final JarFolder folder, final Root root,                       final SourceOption    option, final String... subpath) throws IOException {
		this          (                folder, root.resolve(DEFAULT_SOURCE), root.resolve(DEFAULT_RUNTIME), option,                 subpath);
	}

	public JavaContent(final JarFolder folder, final Root source, final Root runtime, final SourceOption    option, final String... subpath) throws IOException {
		super(folder, option);

		final Path     singleton             = checkForSingleton(source, subpath);
		final int      subpathLength         = subpath.length - (singleton == null ? 0 : 1);
		final String[] subpathMinusSingleton = Arrays.copyOfRange(subpath, 0, subpathLength);

		final Root     sourceSubpath         = source .resolve(subpathMinusSingleton);
		final Root     runtimeSubpath        = runtime.resolve(subpathMinusSingleton);

		LOG.debug("Singleton..: {}", singleton);

		/*
		 * TODO Cross-check Source & Runtime? Presence? Timestamp?
		 */
		walkFileTree("Src", source .path, sourceSubpath .path, DOT_JAVA,  (path2java,  fileNameRelative, fileNameNoSuffix) -> {

			if (singleton == null
			||  singleton.equals(path2java)) {
				LOG.debug("Compare S..: {}", singleton);
				LOG.debug("Compare p..: {}", path2java);

				this.sourcesPathMap.put(fileNameRelative, path2java);
			}
		});
		walkFileTree("Tgt", runtime.path, runtimeSubpath.path, DOT_CLASS, (path2class, fileNameRelative, fileNameNoSuffixNoDollar) -> {
			/*
			 * We only import Classes for which we found some Source...
			 */
			if (this.sourcesPathMap.containsKey(fileNameNoSuffixNoDollar + DOT_JAVA)) {
				this.runtimePathMap.put        (fileNameRelative, path2class);
			}
		});
	}

	private Path checkForSingleton(final Root source, final String... subpath) {

		if (subpath.length == 0) {
			return null;
		}
		final String[] subpathLeading    = Arrays.copyOfRange(subpath, 0, subpath.length - 1 );
		final String   subpathLast       =                    subpath    [subpath.length - 1];

		final Root     possibleSingleton;

		if (subpathLast.endsWith(DOT_JAVA)) {
			/**/       possibleSingleton = source.resolve(subpathLeading).resolve(subpathLast);
		} else {
			/**/       possibleSingleton = source.resolve(subpathLeading).resolve(subpathLast + DOT_JAVA);
		}
		if (Files.isRegularFile(possibleSingleton.path)) { // Yes, its a Singleton
			return              possibleSingleton.path;
		} else {
			return null;
		}
	}

	private static void walkFileTree(final String logPrefix, final Path root, final Path subpath, final String dotSuffix, final TriConsumer<Path, String, String> consumer) throws IOException {
		try {
			Files.walkFileTree(subpath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {

					final String fileNameRelative         = unBackSlash(relativize(root, path));
					final String fileNameNoSuffix         = removeSuffix    (fileNameRelative, dotSuffix);
					final String fileNameNoSuffixNoDollar = removeFromChar  (fileNameNoSuffix, '$');

					if (fileNameNoSuffixNoDollar.equals(fileNameRelative) == false) {
						consumer.accept(path, fileNameRelative, fileNameNoSuffixNoDollar);

						LOG.debug("{}: VISIT.: {} <- {} <- {} L={}", logPrefix, fileNameNoSuffixNoDollar, fileNameRelative, path, attrs.size());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (final IOException e) {
			LOG.error("{}: VISIT.: {} <- {} <- {} L={}", logPrefix, root, dotSuffix);
			throw e;
		}
	}

	@Override
	public Set<String> getClasspathEntries(final String folderName) {
		return   super.getClasspathEntries(             folderName, null);
	}

	@Override
	public void writeSources(final String folderName, final Zipper sourceZipper) {
		this.sourcesPathMap.forEach((name, path) -> {
//			LOG.error("writeSource: {}/t{}", name, path);
			sourceZipper.putEntry(name, path);
		});
	}

	@Override
	public void writeRuntime(final String folderName, final Zipper runtimeZipper) {

		final String folderNameSlash = appendSlash(folderName);

		this.runtimePathMap.forEach((name, path) -> {
//			LOG.error("writeTarget: {}\t{}\t{}", folderNameSlash, name, path);
			runtimeZipper.putEntry(folderNameSlash + name, path);
		});
	}
}
