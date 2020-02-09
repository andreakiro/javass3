package javass3.jass.packed;

import static javass3.bits.Bits32.extract;

import javass3.jass.components.Card.Color;
import javass3.jass.components.Card.Rank;

public class PackedCard {
	
	public static final int INVALID = 0b111111; // 63

	private static final int RANK_SIZE = 4;
	private static final int COLOR_SIZE = 2;

	private static final int RANK_START = 0;
	private static final int COLOR_START = RANK_SIZE;

	private static final int UNUSED_BITS = RANK_SIZE + COLOR_SIZE;

	private PackedCard() {}

	/**
	 * Check if the packed card is a valid card
	 * 
	 * @param pkCard (int)
	 * @return (boolean) : true if pkCard is a valid card
	 */
	public static boolean isValid(int pkCard) {
		int rank = extract(pkCard, RANK_START, RANK_SIZE);
		int other = extract(pkCard, UNUSED_BITS, Integer.SIZE - UNUSED_BITS);
		return (rank >= 0) && (rank < Rank.COUNT) && (other == 0);
	}

	/**
	 * Get the packed card of given rank and color
	 * 
	 * @param c (Color)
	 * @param r (Rank)
	 * @return (int) : the packed card of given rank and color
	 */
	public static int pack(Color c, Rank r) {
		int color = c.ordinal() << RANK_SIZE;
		int rank = r.ordinal();
		return color | rank;
	}

	/**
	 * Get the color of the given packed card
	 * 
	 * @param pkCard (int)
	 * @return (Color) : the color of the given packed card
	 */
	public static Color color(int pkCard) {
		assert isValid(pkCard);
		int color = extract(pkCard, COLOR_START, COLOR_SIZE);
		return Color.ALL.get(color);
	}

	/**
	 * Get the rank of the given packed card
	 * 
	 * @param pkCard (int)
	 * @return (Rank) : the rank of the given packed card
	 */
	public static Rank rank(int pkCard) {
		assert isValid(pkCard);
		int rank = extract(pkCard, RANK_START, RANK_SIZE);
		return Rank.ALL.get(rank);
	}

	/**
	 * Check if the first card is better then the second one
	 * 
	 * @param trump   (Color) : if trump the cards ranking changes
	 * @param pkCardL (int)
	 * @param pkCardR (int)
	 * @return (boolean) : true if the first card is higher/better then the second
	 *         one
	 */
	public static boolean isBetter(Color trump, int pkCardL, int pkCardR) {
		int rankL = rank(pkCardL).ordinal();
		int rankR = rank(pkCardR).ordinal();
		int trumpRankL = rank(pkCardL).trumpOrdinal();
		int trumpRankR = rank(pkCardR).trumpOrdinal();

		Color colorL = color(pkCardL);
		Color colorR = color(pkCardR);

		boolean check = colorL == trump;

		if (colorL == colorR)
			return check ? trumpRankL > trumpRankR : rankL > rankR;
		else
			return check;
	}

	/**
	 * Get the points of a given card
	 * 
	 * @param trump  (Color) : if trump the cards ranking changes
	 * @param pkCard (int)
	 * @return (int) : the points of the given card
	 */
	public static int points(Color trump, int pkCard) {
		Color color = color(pkCard);
		boolean check = color == trump;
		switch (rank(pkCard).ordinal()) {
		case 0:
			;
		case 1:
			;
		case 2:
			return 0;
		case 3:
			return check ? 14 : 0;
		case 4:
			return 10;
		case 5:
			return check ? 20 : 2;
		case 6:
			return 3;
		case 7:
			return 4;
		case 8:
			return 11;
		}
		return 0;
	}

	/**
	 * Overload of toString
	 * 
	 * @param pkCard (int)
	 * @return (String) : textual representation of a card
	 */
	public static String toString(int pkCard) {
		Color color = color(pkCard);
		Rank rank = rank(pkCard);
		return color.toString() + rank.toString();
	}
}