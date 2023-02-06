package de.davelaw.jarinjar.examples;

import java.io.IOException;
import java.nio.file.Paths;

import de.davelaw.jarinjar.content.JavaContent;
import de.davelaw.jarinjar.content.ResourcesContent;
import de.davelaw.jarinjar.content.SourceOption;
import de.davelaw.jarinjar.content.ImportedZipContent;
import de.davelaw.jarinjar.content.JarLibContent;
import de.davelaw.jarinjar.spec.JarSpec;
import de.davelaw.jarinjar.spec.JarSpecFactory;
import de.davelaw.jarinjar.spec.JarSpecFactory.JarFolder;
import de.davelaw.jarinjar.spec.Root;
import de.davelaw.jarinjar.zip.JarInJarManifest;

public class Test {
	/*
	 * This software is intended to complement the Eclipse Jar-in-Jar Runnable JAR export, which does not export Resources correctly.
	 * 
	 * Apart from correcting that deficiency, Jar-in-Jar Helper gives precise control over the Classpath.
	 * Also, the Folder-structure of the generated JAR should be more transparent.
	 * 
	 * It was written on a Java 1.8 JDK & requires a 1.8 or later JRE.
	 * 
	 * This preliminary Version is functionally sound, but the API may be subject to change in the final version.
	 * 
	 * This preliminary Version is distributed in Source-form only.
	 * No 3rd party resources have been included.
	 * Hopefully this will change, after reviewing the various licences.
	 * 
	 * In particular, "jar-in-jar-loader.zip" is required.
	 * Here's how to obtain a copy.
	 * 
	 * How to obtain "jar-in-jar-loader.zip"
	 * -------------------------------------
	 * 
	 * - Right-click on an Eclipse Project
	 * - select "Export..."
	 * - select "Runnable JAR file" (in Java Folder)
	 * - select option 2 : "Package required libraries into generated JAR"
	 * * !! CHECK THE "Save as ANT Script" Checkbox & enter a location for it!!
	 * - select "Finish"
	 * 
	 * You should now find "jar-in-jar-loader.zip" in the location you selected for the ANT Script.
	 * In Eclipse 2022-12, this contained Classes timestamped as 2020-08-04 16:24:14.
	 */
	public static void main(final String[] args) throws IOException {
		/*
		 * Define Root Locations of Workspace, Resources, git-Repo etc...
		 */
		final Root               ws             = new Root(Paths.get("C:", "Users",      "User",    "git",                "jarinjar"));
		final Root               gitRepo        = new Root(Paths.get("C:", "Users",      "User",    ".m2",                "repository"));

		final Root               resources      = ws       .resolve(JavaContent.DEFAULT_RESOURCES);
		final Root               jarInJarZip    = resources.resolve("jarInJarLoader", "jar-in-jar-loader_v2020.08.04_16.24.zip");

		final Root               slf4jAPI       = gitRepo  .resolve("org", "slf4j", "slf4j-api",                    "2.0.4", "slf4j-api-2.0.4.jar");
		final Root               logbackClassic = gitRepo  .resolve("ch",  "qos",   "logback",   "logback-classic", "1.3.5", "logback-classic-1.3.5.jar");
		final Root               logbackCore    = gitRepo  .resolve("ch",  "qos",   "logback",   "logback-core",    "1.3.5", "logback-core-1.3.5.jar");

		/*
		 * Define the Project & Folders...
		 * (the order defines the order on the generated Classpath)
		 */
		final JarSpecFactory     factory        = new JarSpecFactory(Test.class, jarInJarZip);

		final JarFolder          folderApp      = factory.appendJarFolder("app");
		final JarFolder          folderAppRes   = factory.appendJarFolder("app_resources");
		final JarFolder          folderLib      = factory.appendJarFolder("lib");
		final JarFolder          folderJIJ      = factory.appendJarFolder(JarInJarManifest.JAR_IN_JAR_FOLDER);

		/*
		 * Now add some Content (either Directories or individual Files) to the Folders...
		 */
		new   JavaContent       (folderApp,     ws, SourceOption.YES);
		new   ResourcesContent  (folderAppRes,  resources, "jarInJarLoader", "jar-in-jar-loader_v2020.08.04_16.24.zip");

		new   JarLibContent     (folderLib,     slf4jAPI);
		new   JarLibContent     (folderLib,     logbackClassic);
		new   JarLibContent     (folderLib,     logbackCore);

		new   ImportedZipContent(folderJIJ,     jarInJarZip); // TODO Jar-in-Jar may be appended automatically in final version

		/*
		 * Generate the Runnable JAR...
		 */
		final JarSpec            jarSpec = factory.generate();
		/**/                     jarSpec.buildJar(SourceOption.YES, "my_Runnable_Eclipse_JarInJarHelper.jar");
	}
}
