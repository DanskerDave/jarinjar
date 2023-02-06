package de.davelaw.jarinjar.spec;

import static de.davelaw.jarinjar.util.Util.getOrdinalLength;
import static de.davelaw.jarinjar.util.Util.validateIdentifier;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.davelaw.jarinjar.content.Content;
import de.davelaw.jarinjar.zip.JarInJarManifest;

public final class JarSpecFactory {

	public final class JarFolder implements Comparable<JarFolder> {

		public  final List<Content> contentList = new LinkedList<>();

		private final String folderName;
		public  /**/  String folderNamePrefixed;

		private JarFolder(final String folderName) {
			this.folderName = normaliseFolderName(folderName);
		}
		private String normaliseFolderName(final String baseName) {
			if (baseName == null
			||  baseName.isEmpty()) {
				return "";
			} else {
				return validateIdentifier(baseName);
			}
		}

		public boolean isNamed() {
			return this.folderName.isEmpty() == false;
		}

		public void appendContent(final Content content) {
			this.contentList.add(content);
		}

		public Set<String> getClasspathEntries() {
			return         getClasspathEntries(this.folderName);
		}
		public Set<String> getClasspathEntries(final String folderName) {

			final Stream<Set<String>> cpEntrySet = this.contentList.stream().map(content -> content.getClasspathEntries(folderName));

			return                    cpEntrySet.collect(LinkedHashSet::new, LinkedHashSet::addAll, LinkedHashSet::addAll);
		}

		@Override
		public int compareTo(final JarFolder that) {
			return this.folderName.compareTo(that.folderName);
		}
	}

	public  final Set<JarFolder> jarFolderSet = new LinkedHashSet<>();
	private final String         mainClassName;
//	private final Root           jarInJarZip;

	public JarSpecFactory(final Class<?> mainClass, final Root jarInJarZip) {
		this.mainClassName  = mainClass.getName();
//		this.jarInJarZip    = jarInJarZip;
	}

	public Set<String> getClasspathEntries() {

		final String        ordinalFormat = "%0" + this.getMaxClasspathOrdinalLength() + "d";

		final AtomicInteger aiCP          = new AtomicInteger();
		final AtomicInteger aiXT          = new AtomicInteger();

		final Stream<Set<String>> cpEntrySet = this.jarFolderSet.stream()
				.map(       jbf -> {
					if (    jbf.folderName.isEmpty()) {
						/**/jbf.folderNamePrefixed = "";
					} else {
						if (jbf.getClasspathEntries(ordinalFormat).isEmpty()) {
							jbf.folderNamePrefixed = "xt" + String.format(ordinalFormat, aiXT.getAndIncrement()) + "_" + jbf.folderName;
						} else {
							jbf.folderNamePrefixed = "cp" + String.format(ordinalFormat, aiCP.getAndIncrement()) + "_" + jbf.folderName;
						}
					}
					if (    jbf.folderName.equals   (JarInJarManifest.JAR_IN_JAR_FOLDER)) {
						/**/jbf.folderNamePrefixed = JarInJarManifest.JAR_IN_JAR_FOLDER;
					}
					return  jbf.getClasspathEntries(jbf.folderNamePrefixed);
				});

		return cpEntrySet.collect(LinkedHashSet::new, LinkedHashSet::addAll, LinkedHashSet::addAll);
	}

	private int getMaxClasspathOrdinalLength() {

		final Set<JarFolder> namedFolders = this.jarFolderSet.stream().filter(JarFolder::isNamed).collect(Collectors.toSet());
		/*
		 * Initial assumption.: everything's ON the Classpath...
		 * (except the optional unnamed Folder which, from a numbering point-of-view, is not relevant)
		 */
		final Set<JarFolder> onClasspath  = new HashSet<>(namedFolders);
		final Set<JarFolder> offClasspath = new HashSet<>();
		/*
		 * Now move non-Classpath entries from "on" to "off"...
		 */
		namedFolders.forEach                    (jarFolder -> {

			if (jarFolder.getClasspathEntries().isEmpty()) {

				/**/         onClasspath .remove(jarFolder);
				/**/         offClasspath.add   (jarFolder);
			}
		});
		return getOrdinalLength(Math.max(onClasspath.size(), offClasspath.size()) - 1 /* less 1 because we're 0-based */);
	}

	public  JarFolder appendJarFolder() {
		return        appendJarFolder("");
	}
	public  JarFolder appendJarFolder(String folderName) {

		folderName = folderName == null  ?  ""  :  folderName;

		final JarFolder           newJarFolder = new JarFolder(folderName);

		if (this.jarFolderSet.add(newJarFolder)) {
			return                newJarFolder;
		}
		throw new NullPointerException("A JarFolder with this name already exists: " + folderName);
	}

	public  JarSpec generate() {

		final Set<String> resourceClasspath = this.getClasspathEntries();
		final Manifest    manifest          = new JarInJarManifest(resourceClasspath, this.mainClassName);

		final Map<String, List<Content>>  modifiableContentMap = new LinkedHashMap<>();

		this.jarFolderSet.forEach(jarFolder -> {

			final         List<Content>     jarFolderContentListClone = new LinkedList<>(jarFolder.contentList);

			modifiableContentMap   .put    (jarFolder.folderNamePrefixed, unmodifiableList(jarFolderContentListClone));
		});

		return new JarSpec(unmodifiableMap(modifiableContentMap), manifest);
	}
}
