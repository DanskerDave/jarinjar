package de.davelaw.jarinjar.zip;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarInJarManifest extends Manifest {

	private static final Logger LOG                             = LoggerFactory.getLogger(JarInJarManifest.class);

	/**
	 * This FolderName is reserved for the Jar-In-Jar Boot-Loader.<br>
	 * It should not be appended to the Classpath.
	 */
	public  static final String JAR_IN_JAR_FOLDER               =                          "jarinjarloader";
	private static final String JAR_IN_JAR_MAIN_CLASS           = "org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader";

	private static final String JAR_IN_JAR_ATTR_RSRC_CLASS_PATH = "Rsrc-Class-Path";
	private static final String JAR_IN_JAR_ATTR_RSRC_MAIN_CLASS = "Rsrc-Main-Class";


	public JarInJarManifest(final Set<String> resourceClasspath, final Class<?> resourceMainClass) {
		this               (                  resourceClasspath,                resourceMainClass.getName());
	}

	public JarInJarManifest(final Set<String> resourceClasspath, final String   resourceMainClass) {
		this               (                  resourceClasspath,                resourceMainClass,              JAR_IN_JAR_FOLDER);
	}

	public JarInJarManifest(final Set<String> resourceClasspath, final String   resourceMainClass, final String jarInJarFolder) {
		this               (                  resourceClasspath,                resourceMainClass,              jarInJarFolder,              JAR_IN_JAR_MAIN_CLASS);
	}

	public JarInJarManifest(final Set<String> resourceClasspath, final String   resourceMainClass, final String jarInJarFolder, final String jarInJarMainClass) {
		super();

		final String resourceClasspathString = resourceClasspath.stream().collect(Collectors.joining(" "));

		try {
			/*
			 * Try to substitute the Manifest Main Attributes Map with a "Linked" Map which preserves put-order...
			 * (any existing contents - in our case none - will be imported in their iterator order)
			 */
			final Field      attributesMapField = Attributes.class  .getDeclaredField("map");
			/**/             attributesMapField.setAccessible(true);

			final Field      manifestAttrField  = Manifest  .class  .getDeclaredField("attr");
			/**/             manifestAttrField .setAccessible(true);

			final Attributes manifestAttrValue  = (Attributes) manifestAttrField .get(this);
			final Map<?,?>   attributesMapValue = (Map<?,?>)   attributesMapField.get(manifestAttrValue);
			/**/                                               attributesMapField.set(manifestAttrValue, new LinkedHashMap<>(attributesMapValue));
		}
		catch (final Throwable e) {
			LOG.info("jar IN jar.: {} {}", "failed to replace Attributes Map. (NOT Critical !!)", e.getMessage());
		}

		final Attributes   attr = this.getMainAttributes();
		/**/               attr.put(          Attributes.Name.MANIFEST_VERSION,                 "1.0");

		/**/               attr.put(          Attributes.Name.CLASS_PATH,                       jarInJarFolder + '/');
		/**/               attr.put(          Attributes.Name.MAIN_CLASS,                       jarInJarMainClass);

		/**/               attr.put(      new Attributes.Name(JAR_IN_JAR_ATTR_RSRC_MAIN_CLASS), resourceMainClass);
		/**/               attr.put(      new Attributes.Name(JAR_IN_JAR_ATTR_RSRC_CLASS_PATH), resourceClasspathString);
	}
}
