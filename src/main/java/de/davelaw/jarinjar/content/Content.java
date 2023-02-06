package de.davelaw.jarinjar.content;

import static de.davelaw.jarinjar.util.Util.appendSlash;

import java.util.Collections;
import java.util.Set;

import de.davelaw.jarinjar.spec.JarSpecFactory.JarFolder;
import de.davelaw.jarinjar.zip.Zipper;

public abstract class Content {

	private static final String       DOT   = ".";

	public         final SourceOption sourceOption;

	protected Content(final JarFolder folder) {
		this(folder, SourceOption.NO);
	}
	protected Content(final JarFolder folder, final SourceOption sourceOption) {
		this.sourceOption = sourceOption;

		folder.appendContent(this);
	}

	/**
	 * Returns a
	 * {@link Set}    of
	 * {@link String} of Classpath entries for this
	 * {@link Content}.
	 * <p>
	 * If this {@code Content} is not required on the Classpath, the {@code  Set} will be empty.<br>
	 * Otherwise, the {@code  Set} will usually consist of a Singleton,
	 * but allows for future {@code Content} with multiple entries.
	 * <p>
	 * {@code null} & {@code BLANK} Foldernames will be normalised to {@code '.'}.<br>
	 * {@code '/' will be appended to the Foldername}.
	 * 
	 * @param folderName  either the Original value or the Prefixed value
	 * @return
	 */
	public abstract Set<String> getClasspathEntries(final String folderName);

	/**
	 * Default Classpath Generator which should be good for most Subclasses.
	 * <p>
	 * Returns a
	 * {@link Set}    of
	 * {@link String} of Classpath entries for this
	 * {@link Content}.
	 * <p>
	 * If this {@code Content} is not required on the Classpath, the {@code  Set} will be empty.<br>
	 * Otherwise, the {@code  Set} will usually consist of a Singleton,
	 * but allows for future {@code Content} with multiple entries.
	 * 
	 * @param folderName  either the Original value or the Prefixed value
	 * @param jarFileName if non-null, it will be slash-appended to folderName 
	 * @return
	 */
	protected final Set<String> getClasspathEntries(      String folderName, final String jarFileName) {

		folderName = folderName == null  ?  DOT  :  folderName.isEmpty()  ?  DOT  :  folderName;

		folderName = appendSlash(folderName); // "dotOrPrefix" -> "dotOrPrefix/"

		if (jarFileName == null) {
			return Collections.singleton(folderName);
		} else {
			return Collections.singleton(folderName + jarFileName);
		}
	}

	public abstract void writeSources(final String folderName, final Zipper zipper);
	public abstract void writeRuntime(final String folderName, final Zipper zipper);
}
