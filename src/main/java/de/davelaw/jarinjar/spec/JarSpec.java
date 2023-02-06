package de.davelaw.jarinjar.spec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.davelaw.jarinjar.content.Content;
import de.davelaw.jarinjar.content.SourceOption;
import de.davelaw.jarinjar.zip.ByteArrayEntry;
import de.davelaw.jarinjar.zip.JarInJarManifest;
import de.davelaw.jarinjar.zip.Zipper;

public class JarSpec {

	private static final Logger                     LOG = LoggerFactory.getLogger(JarSpec.class);

	public         final Map<String, List<Content>> contentMap;
	public         final Manifest                   manifest;

	public JarSpec(final Map<String, List<Content>> contentMap, final Manifest manifest) {
		this.contentMap = contentMap;
		this.manifest   = manifest; // TODO buildManifest  (null, null);
	}

	public Manifest buildManifest(final Set<String> resourceClasspath, final String resourceMainClass) {

		return new JarInJarManifest(resourceClasspath, resourceMainClass);
	}

	public void buildJar(                                 final String pathFirst, final String... pathMore) {
		/**/    buildJar(      SourceOption.NO,                        pathFirst,                 pathMore);
	}
	public void buildJar(final SourceOption sourceOption, final String pathFirst, final String... pathMore) {
		/**/    buildJar(                   sourceOption, Paths.get   (pathFirst,                 pathMore));
	}

	public void buildJar(                                 final Path generatedJarFilename) {
		/**/    buildJar(      SourceOption.NO,                      generatedJarFilename);
	}
	public String getSourceZipFilename(final String folderName) {
		if (folderName.isEmpty()) {
			return "src"               + ".zip";
		} else {
			return "src_" + folderName + ".zip";
		}
	}
	public void buildJar(final SourceOption globalSourceOption, final Path generatedJarFilename) {
		/*
		 * FIRST Phase.: build Source-Zips incore, folder-by-folder as required, for inclusion in the JAR...
		 */
		final List<ByteArrayEntry> sourceZipList = new LinkedList<>();

		for(final Entry<String, List<Content>> entry         : this.contentMap.entrySet()) {

			final             String                folderName   = entry.getKey();
			final             ByteArrayOutputStream baos         = new ByteArrayOutputStream();
			try {
				final         Zipper                sourceZipper = Zipper.createZip(baos, zipper -> {

					for(final Content               content      : entry.getValue()) {

						if (SourceOption.isExported(globalSourceOption, content.sourceOption)) {
							content.writeSources(folderName, zipper);
						}
					}
				});
				if (sourceZipper.getEntryCount() > 0) {
					sourceZipList.add(new ByteArrayEntry(getSourceZipFilename(folderName), sourceZipper.getMaxFileTime(), baos.toByteArray()));
				}
			}
			catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * SECOND Phase.: build the JAR, including any Source-Zips generated above...
		 */
		try {
			final         OutputStream                 outputStream  = Files.newOutputStream(generatedJarFilename);
			final         Zipper                       runtimeZipper = Zipper.createJar(this.manifest, outputStream, zipper -> {
				/*
				 * Include the Source-Zips...
				 */
				for(final ByteArrayEntry               byteArray     : sourceZipList) {
					zipper.putEntry(byteArray);
				}
				/*
				 * Copy the Runtime Content to the JAR, Folder-by-Folder, Content-by-Content...
				 */
				for(final Entry<String, List<Content>> entry      : contentMap.entrySet()) {

					final       String                 folderName = entry.getKey();

					for(final                Content   content    : entry.getValue()) {

						content.writeRuntime(folderName, zipper);
					}
				}
			});
			LOG.info("jar IN jar.: {} files written to {}", runtimeZipper.getEntryCount(), generatedJarFilename);
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
