package javass3.bits;

import static javass3.Preconditions.checkArgument;

public class Bits32 {
	
	private Bits32() {}

	/**
	 * Create a mask from start (inc) to start + size (exc)
	 * 
	 * @param start (int) : where the mask begins
	 * @param size  (int) : the size of the mask
	 * @throws IllegalArgumentException
	 * @return (int) : an integer which is a mask the if loop is to prevent the case
	 *         when size is 32 i.e there is no shift
	 */
	public static int mask(int start, int size) {
		checkArgument((start >= 0) && (size >= 0) && (start + size <= Integer.SIZE));
		if (size == Integer.SIZE)
			return 0xFFFF_FFFF; // -1
		int mask = (1 << size) - 1;
		return mask << start;
	}

	/**
	 * Create an integer that has the value of the start to start + size bit string
	 * on his low weight bits
	 * 
	 * @param bits  (int) : the bit string we change
	 * @param start (int) : where the mask begins
	 * @param size  (int) : the size of the mask
	 * @throws IllegalArgumentException
	 * @return (int) : that integer
	 */
	public static int extract(int bits, int start, int size) {
		checkArgument((start >= 0) && (size >= 0) && (start + size <= Integer.SIZE));
		int mask = mask(start, size);
		return (bits & mask) >>> start;
	}

	/**
	 * Pack values v1 and v2 in an integer
	 * 
	 * @param v1 (int) : first packed value
	 * @param s1 (int) : first s1 bits in which v1 is packed
	 * @param v2 (int) : second packed value
	 * @param s2 (int) : next s2 bits in which v2 is packed
	 * @throws IllegalArgumentException
	 * @return (int) : that integer
	 */
	public static int pack(int v1, int s1, int v2, int s2) {
		checkArgument(s1 + s2 <= Integer.SIZE);
		checkPair(v1, s1);
		checkPair(v2, s2);
		return v1 | (v2 << s1);
	}

	/**
	 * Pack values v1, v2 and v3 in an integer
	 * 
	 * @param v1 (int) : first packed value
	 * @param s1 (int) : first s1 bits in which v1 is packed
	 * @param v2 (int) : second packed value
	 * @param s2 (int) : next s2 bits in which v2 is packed
	 * @param v3 (int) : the third packed value
	 * @param s3 (int) : next s3 bits in which v3 is packed
	 * @throws IllegalArgumentException
	 * @return (int) : that integer
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
		checkArgument(s1 + s2 + s3 <= Integer.SIZE);
		checkPair(v3, s3);
		int p1 = pack(v1, s1, v2, s2);
		return p1 | (v3 << (s2 + s1));
	}

	/**
	 * Pack values v1, v2, v3, v4, v5, v6, and v7 in an integer
	 * 
	 * @param v1 (int) : first packed value
	 * @param s1 (int) : first s1 bits in which v1 is packed
	 * @param v2 (int) : second packed value
	 * @param s2 (int) : next s2 bits in which v2 is packed
	 * @param v3 (int) : the third packed value
	 * @param s3 (int) : next s3 bits in which v3 is packed
	 * @param v4 (int) : fourth packed value
	 * @param s4 (int) : next s4 bits in which v4 is packed
	 * @param v5 (int) : fifth packed value
	 * @param s5 (int) : next s5 bits in which v5 is packed
	 * @param v6 (int) : sixth packed value
	 * @param s6 (int) : next s6 bits in which v6 is packed
	 * @param v7 (int) : seventh packed value
	 * @param s7 (int) : next s7 bits in which v6 is packed
	 * @throws IllegalArgumentException
	 * @return (int) : that integer
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4, int v5, int s5, int v6,
			int s6, int v7, int s7) {
		checkArgument(s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);
		checkPair(v7, s7);
		int p1 = pack(v1, s1, v2, s2, v3, s3);
		int p2 = pack(v4, s4, v5, s5, v6, s6) << s1 + s2 + s3;
		int p3 = v7 << (s1 + s2 + s3 + s4 + s5 + s6);
		return p1 | p2 | p3;
	}

	/**
	 * Check if the size is between 1 (inc) and 31 (inc) and if the value doesn't
	 * takes more bits then the specified size
	 * 
	 * @param v (int) : bit string value
	 * @param s (int) : size
	 * @throws IllegalArgumentException
	 */
	private static void checkPair(int v, int s) {
		checkArgument((s >= 1) && (s < Integer.SIZE));
		checkArgument((Integer.SIZE - Integer.numberOfLeadingZeros(v)) <= s);
	}
}
