package javass3.jass.packed;

import static javass3.bits.Bits64.extract;
import static javass3.jass.packed.PackedCard.isBetter;
import static javass3.jass.packed.PackedCard.pack;

import java.util.StringJoiner;

import javass3.jass.components.Card.Color;
import javass3.jass.components.Card.Rank;

public class PackedCardSet {
	
	public static final long EMPTY = 0l;
	public static final long ALL_CARDS = allCards();

	private static final int USED_BITS = 9;
	private static final int UNUSED_BITS = 7;
	private static final int COLOR_SIZE = USED_BITS + UNUSED_BITS;

	private static final long[][] tabTrumpAbove = tableTrumpAbove();
	private static final long[] tabColor = tableSubsetOfColor();

	private PackedCardSet() {
	}

	/**
	 * Check if the long containing the card set is valid, ie none of the 28 unused
	 * bits are on
	 * 
	 * @param pkCardSet (long) : the cardset
	 * @return (boolean) : true iff pkCardSet is valid
	 */
	public static boolean isValid(long pkCardSet) {
		for (int c = 0; c < Long.SIZE; c += COLOR_SIZE) {
			if (extract(pkCardSet, c + USED_BITS, UNUSED_BITS) != 0)
				return false;
		}
		return true;
	}

	/**
	 * Get the packed cardset containing all the cards stronger then the given
	 * packed card, knowing it's of trump color
	 * 
	 * @param pkCard (int)
	 * @return (long) : pkCardset containing all the cards stronger then pkCard
	 *         given it's trump
	 */
	public static long trumpAbove(int pkCard) {
		return tabTrumpAbove[PackedCard.color(pkCard).ordinal()][PackedCard.rank(pkCard).ordinal()];
	}

	/**
	 * Get the packed cardset containing only the given packed card
	 * 
	 * @param pkCard (int)
	 * @return (long) : packed cardset containing only the packed card
	 */
	public static long singleton(int pkCard) {
		assert PackedCard.isValid(pkCard);
		return 1L << pkCard;
	}

	/**
	 * Check if the pkCardSet is empty, ie all 64 bits off
	 * 
	 * @param pkCardSet (long)
	 * @return (boolean) : true iff pkCardSet is empty
	 */
	public static boolean isEmpty(long pkCardSet) {
		assert isValid(pkCardSet);
		return pkCardSet == EMPTY;
	}

	/**
	 * Get the number of cards contained in the given packed cardset, ie the number
	 * of bits "on", called size of the set
	 * 
	 * @param pkCardSet (long)
	 * @return (int) : size of the packed cardset
	 */
	public static int size(long pkCardSet) {
		assert isValid(pkCardSet);
		return Long.bitCount(pkCardSet);
	}

	/**
	 * Get a packed card of the packed cardset given an index
	 * 
	 * @param pkCardSet (long)
	 * @param index     (int)
	 * @return (int) : the packed card
	 */
	public static int get(long pkCardSet, int index) {
		assert isValid(pkCardSet);
		assert (index >= 0) && (index < size(pkCardSet));
		for (int i = 0; i < index; i++)
			pkCardSet = ~Long.lowestOneBit(pkCardSet) & pkCardSet;
		return Long.numberOfTrailingZeros(pkCardSet);
	}

	/**
	 * Add the given pkCard in the given pkCardSet
	 * 
	 * @param pkCardSet (long)
	 * @param pkCard    (int)
	 * @return (long) : new packed cardset with an added card
	 */
	public static long add(long pkCardSet, int pkCard) {
		return union(singleton(pkCard), pkCardSet);
	}

	/**
	 * Remove the given pkCard in the given pkCardSet
	 * 
	 * @param pkCardSet (long)
	 * @param pkCard    (int)
	 * @return (long) : new packed cardset with a removed card
	 */
	public static long remove(long pkCardSet, int pkCard) {
		return intersection(complement(singleton(pkCard)), pkCardSet);
	}

	/**
	 * Check if the given pkCardSet contains the given pkCard
	 * 
	 * @param pkCardSet (long)
	 * @param pkCard    (int)
	 * @return (boolean) : true iff pkCardSet contains pkCard
	 */
	public static boolean contains(long pkCardSet, int pkCard) {
		return intersection(singleton(pkCard), pkCardSet) != EMPTY;
	}

	/**
	 * Get the complements of the given packed cardset
	 * 
	 * @param pkCardSet (long)
	 * @return (long) : new packed complement cardset
	 */
	public static long complement(long pkCardSet) {
		assert isValid(pkCardSet);
		return ~pkCardSet & ALL_CARDS;
	}

	/**
	 * Get the union of the two given packed cardsets
	 * 
	 * @param pkCardSet1 (long)
	 * @param pkCardSet2 (long)
	 * @return (long) : new unified packed cardset
	 */
	public static long union(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1);
		assert isValid(pkCardSet2);
		return pkCardSet1 | pkCardSet2;
	}

	/**
	 * Get the intersection of the two given packed cardsets
	 * 
	 * @param pkCardSet1 (long)
	 * @param pkCardSet2 (long)
	 * @return (long) : new intersected packed cardset
	 */
	public static long intersection(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1);
		assert isValid(pkCardSet2);
		return pkCardSet1 & pkCardSet2;
	}

	/**
	 * Get the difference between the first and the second packed cardsets
	 * 
	 * @param pkCardSet1 (long)
	 * @param pkCardSet2 (long)
	 * @return (long) : new packed cardset
	 */
	public static long difference(long pkCardSet1, long pkCardSet2) {
		return intersection(complement(pkCardSet2), pkCardSet1);
	}

	/**
	 * Get a packed cardset containing all the cards of a color from the given set
	 * 
	 * @param pkCardSet (long)
	 * @param color     (Color)
	 * @return (long) : new packed cardset
	 */
	public static long subsetOfColor(long pkCardSet, Color color) {
		return intersection(pkCardSet, tabColor[color.ordinal()]);
	}

	/**
	 * Overload of toString()
	 * 
	 * @param pkCardSet (long)
	 * @return (String) : textual representation of a cardset
	 */
	public static String toString(long pkCardSet) {
		assert isValid(pkCardSet);
		StringJoiner j = new StringJoiner(",", "{", "}");
		for (int i = 0; i < size(pkCardSet); i++)
			j.add(PackedCard.toString(get(pkCardSet, i)));
		return j.toString();
	}

	/**
	 * Create a packed cardset containing all cards
	 * 
	 * @return (long) : full packed cardset
	 */
	private static long allCards() {
		long allCards = EMPTY;
		for (Color c : Color.ALL) {
			for (Rank r : Rank.ALL)
				allCards = union(allCards, singleton(PackedCard.pack(c, r)));
		}
		return allCards;
	}

	/**
	 * Create the array for trumpAbove function
	 * 
	 * @return (long[][]) : trump above array
	 */
	private static long[][] tableTrumpAbove() {
		long[][] tab = new long[Color.COUNT][Rank.COUNT];
		for (Color color : Color.ALL) {
			for (Rank rank : Rank.ALL) {
				long bestCardSet = EMPTY;
				for (Rank trump : Rank.ALL) {
					if (isBetter(color, pack(color, trump), pack(color, rank)))
						bestCardSet = add(bestCardSet, pack(color, trump));
					tab[color.ordinal()][rank.ordinal()] = bestCardSet;
				}
			}
		}
		return tab;
	}

	/**
	 * Create the array for subsetOfColor function
	 * 
	 * @return (long[]) : subset of color array
	 */
	private static long[] tableSubsetOfColor() {
		long[] tab = new long[Color.COUNT];
		for (Color color : Color.ALL) {
			long mask = EMPTY;
			for (Rank rank : Rank.ALL)
				mask = union(mask, singleton(PackedCard.pack(color, rank)));
			tab[color.ordinal()] = mask;
		}
		return tab;
	}
}