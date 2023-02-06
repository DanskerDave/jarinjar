package de.davelaw.jarinjar.util;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class Util {

	private static final char    BACKSLASH_CHAR   = '\\';
	private static final char    SLASH_CHAR       = '/';
	private static final String  EMPTY            = new String();

	private static final Pattern PTN_IDENTIFIER;
	/**/    static {
		/**/       final char    UNDERSCORE       = '_';
		/**/       final String  RGX_MIDDLE_CHARS = "["          + UNDERSCORE       + ']';
		/**/       final String  RGX_FIRST        = "[a-zA-Z"                       + ']';
		/**/       final String  RGX_MIDDLE       = "[a-zA-Z0-9" + RGX_MIDDLE_CHARS + ']';
		/**/       final String  RGX_LAST         = "[a-zA-Z0-9"                    + ']';

		/**/       final String  qty_exactly_once = "{1}";
		/**/       final char    qty_zero_or_more = '*';
		/**/       final char    qty_once_or_more = '+';
		/**/       final char    or               = '|';

		/**/       final String  RGX_ID_SIMPLE    = RGX_FIRST + qty_once_or_more;
		/**/       final String  RGX_ID_COMPLEX   = RGX_FIRST + qty_exactly_once + RGX_MIDDLE + qty_zero_or_more + RGX_LAST + qty_exactly_once;

		/**/                     PTN_IDENTIFIER   = Pattern.compile(RGX_ID_SIMPLE + or + RGX_ID_COMPLEX);
	}

	private Util() {/* Please do not instantiate */}

	/**
	 * Validates an Identifier.
	 * <p>
	 * It must begin with an alpha char.<br>
	 * It may be followed by 0 or more alpha chars or digits.<br>
	 * Additionally, <i>embedded</i> Underscores are permitted.<br>
	 * (Chars may be mixed UPPER- and lower-case)
	 * 
	 * @param name
	 * @return
	 */
	public static String validateIdentifier(final String name) {
		if (PTN_IDENTIFIER.matcher(name).matches()) {
			return                 name;
		}
		throw new IllegalArgumentException("Name contains illegal characters.: " + name);
	}

	/**
	 * Appends {@code  "/"} to a {@code  String}.<br>
	 * (for null & blank Strings a blank String is returned)
	 * 
	 * @param prefix
	 * @return
	 */
	public static String appendSlash(final String prefix) {

		return prefix == null ? EMPTY : prefix.isEmpty() ? EMPTY : prefix + SLASH_CHAR;
	}

	public static int getOrdinalLength(final int value) {

		return value == 0  ?  1  :  1 + (int) Math.log10(value);
	}

	/**
	 * Returns the Path of subPath relative to parent.
	 * 
	 * @param parent
	 * @param subPath
	 * @return
	 * 
	 * @see  Path#relativize(Path)
	 */
	public static String relativize(final Path parent, final Path subPath) {

		return parent.relativize(subPath).toString();
	}

	/**
	 * Removes trailing Characters starting with the first occurrence of delimiter.
	 * 
	 * @param string
	 * @param delimiter
	 * @return
	 */
	public static String removeFromChar(final String string, final char delimiter) {

		final int dollarAt = string.indexOf(delimiter);

		if (dollarAt < 0) {
			return string;
		} else {
			return string.substring(0, dollarAt);
		}
	}

	/**
	 * Removes the supplied Suffix (if present) from the end of a String.
	 * 
	 * @param string
	 * @param suffix
	 * @return
	 */
	public static String removeSuffix(final String string, final String suffix) {

		if (string.endsWith(suffix)) {
			return string.substring(0, string.length() - suffix.length());
		} else {
			return string;
		}
	}

	/**
	 * Replaces Backslashes in a String with (Forward)-Slash.
	 * 
	 * @param path
	 * @return
	 */
	public static String unBackSlash(final String path) {

		return path.replace(BACKSLASH_CHAR, SLASH_CHAR);
	}
}
