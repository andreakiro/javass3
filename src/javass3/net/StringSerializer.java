package javass3.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class StringSerializer {
	
	private final static int BASIS = 16;
	private final static int TRUE_VALUE = 1;
	private final static int FALSE_VALUE = 0;

	/**
	 * Convert an integer into a base 16 representation string
	 * 
	 * @param i (int)
	 * @return (String) : the hexadecimal representation of the integer
	 */
	public static String serializeInt(int i) {
		return Integer.toUnsignedString(i, BASIS);
	}

	/**
	 * Convert from an hexadecimal representation string into an integer
	 * 
	 * @param s (String) : hexadecimal representation of an integer
	 * @return (int) : base 10 representation of the integer
	 */
	public static int deserializeInt(String s) {
		return Integer.parseUnsignedInt(s, BASIS);
	}

	/**
	 * Convert a long into a base 16 representation string
	 * 
	 * @param l (long)
	 * @return (String) : the hexadecimal representation of the integer
	 */
	public static String serializeLong(long l) {
		return Long.toUnsignedString(l, BASIS);
	}

	/**
	 * Convert from an hexadecimal representation string into a long
	 * 
	 * @param s (String) : hexadecimal representation of a long
	 * @return (long) : base 10 representation of the long
	 */
	public static long deserializeLong(String s) {
		return Long.parseLong(s, BASIS);
	}

	/**
	 * Encode a string in UTF_8 representation into a base 64 string
	 * 
	 * @param s (String)
	 * @return (String) : encoded string in base 64
	 */
	public static String serializeString(String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decode a string in base 64 into a string in UTF_8 representation
	 * 
	 * @param s (String) : encoded string in base 64
	 * @return (String) : decoded string in UTF_8
	 */
	public static String deserializeString(String s) {
		byte[] b = Base64.getDecoder().decode(s);
		return new String(b, StandardCharsets.UTF_8);
	}

	/**
	 * Combine all strings into a unique one separated by the separator
	 * 
	 * @param sep (String) : the separator character
	 * @param s   (String) : all the given strings
	 * @return (String) : the combined string
	 */
	public static String combine(String sep, String... s) {
		return String.join(sep, s);
	}

	/**
	 * Split a string into all strings that compose it and put them into an array
	 * 
	 * @param sep (String) : the separator character
	 * @param s   (String) : the combined string
	 * @return (String[]) : array containing all strings
	 */
	public static String[] split(String sep, String s) {
		return s.split(sep);
	}

	// bonus : serializer for boolean

	/**
	 * Encode a boolean in a int base 16 string representation
	 * 
	 * @param b (boolean)
	 * @return String
	 */
	public static String serializeBoolean(boolean b) {
		return Integer.toUnsignedString(b ? TRUE_VALUE : FALSE_VALUE, BASIS);
	}

	/**
	 * Decode a boolean from a int base 16 string
	 * 
	 * @param s (String)
	 * @return boolean
	 */
	public static boolean deserializeBoolean(String s) {
		return Integer.parseUnsignedInt(s, BASIS) == TRUE_VALUE;
	}
}