package javass3.bits;

import static javass3.Preconditions.checkArgument;

public class Bits64 {
	
	private Bits64() {}

	/**
	 * Create a mask from start (inc) to start + size (exc)
	 * 
	 * @param start (int) : where the mask begins
	 * @param size  (int) : the size of the mask
	 * @throws IllegalArgumentException
	 * @return (long) : a long which is a mask the if loop is to prevent the case
	 *         when size is 32 i.e there is no shift
	 */
	public static long mask(int start, int size) {
		checkArgument((start >= 0) && (size >= 0) && (start + size <= Long.SIZE));
		if (size == Long.SIZE)
			return 0xFFFF_FFFF; // -1
		long mask = (1L << size) - 1;
		return mask << start;
	}

	/**
	 * Create a long that has the value of the start to start + size bit string on
	 * his low weight bits
	 * 
	 * @param bits  (long) : the bit string we change
	 * @param start (int) : where the mask begins
	 * @param size  (int) : the size of the mask
	 * @throws IllegalArgumentException
	 * @return (long) : that long
	 */
	public static long extract(long bits, int start, int size) {
		checkArgument((start >= 0) && (size >= 0) && (start + size <= Long.SIZE));
		long mask = mask(start, size);
		return (bits & mask) >>> start;
	}

	/**
	 * Pack values v1 and v2 in a long
	 * 
	 * @param v1 (long) : first packed value
	 * @param s1 (int) : first s1 bits in which v1 is packed
	 * @param v2 (long) : second packed value
	 * @param s2 (int) : next s2 bits in which v2 is packed
	 * @throws IllegalArgumentException
	 * @return (long) : that long
	 */
	public static long pack(long v1, int s1, long v2, int s2) {
		checkArgument(s1 + s2 <= Long.SIZE);
		checkPair(v1, s1);
		checkPair(v2, s2);
		return v1 | (v2 << s1);
	}

	/**
	 * Check if the size is between 1 (inc) and 31 (inc) and if the value doesn't
	 * takes more bits then the specified size
	 * 
	 * @param v (long) : bit string value
	 * @param s (int) : size
	 * @throws IllegalArgumentException
	 */
	private static void checkPair(long v, int s) {
		checkArgument((s >= 1) && (s < Long.SIZE));
		checkArgument((Long.SIZE - Long.numberOfLeadingZeros(v)) <= s);
	}
}
