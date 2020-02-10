package javass3.jass.packed;

import static javass3.bits.Bits32.extract;
import static javass3.jass.Jass.LAST_TRICK_ADDITIONAL_POINTS;
import static javass3.jass.packed.PackedCardSet.EMPTY;
import static javass3.jass.packed.PackedCardSet.add;
import static javass3.jass.packed.PackedCardSet.complement;
import static javass3.jass.packed.PackedCardSet.difference;
import static javass3.jass.packed.PackedCardSet.get;
import static javass3.jass.packed.PackedCardSet.intersection;
import static javass3.jass.packed.PackedCardSet.remove;
import static javass3.jass.packed.PackedCardSet.singleton;
import static javass3.jass.packed.PackedCardSet.subsetOfColor;
import static javass3.jass.packed.PackedCardSet.trumpAbove;
import static javass3.jass.packed.PackedCardSet.union;

import java.util.StringJoiner;

import javass3.bits.Bits32;
import javass3.jass.Jass;
import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.Card.Rank;
import javass3.jass.ids.PlayerId;

public class PackedTrick {
	
	public static final int INVALID = 0xFFFF_FFFF; // -1

	private static final int SIZE_CARD = 6;
	private static final int SIZE_INDEX = 4;
	private static final int SIZE_PLAYER = 2;
	private static final int SIZE_TRUMP = 2;

	private static final int START_INDEX = 24;
	private static final int START_PLAYER = 28;
	private static final int START_TRUMP = 30;

	private static final int MAX_CARDS = 4;

	private PackedTrick() {
	}

	/**
	 * Check if the given packed trick is valid, ie index between 0 (inc) & 8 (inc),
	 * and if the invalid cards are put in the high weight bits
	 * 
	 * @param pkTrick (int)
	 * @return (boolean) : true iff pkTrick is valid
	 */
	public static boolean isValid(int pkTrick) {
		int index = extract(pkTrick, START_INDEX, SIZE_INDEX);
		return (index >= 0) && (index < Jass.TRICKS_PER_TURN) && checkValidityOrder(pkTrick);
	}

	/**
	 * Check if the invalids cards are put in the high weight bits
	 * 
	 * @param pkTrick (int)
	 * @return (boolean) : true iff the order is valid
	 */
	private static boolean checkValidityOrder(int pkTrick) {
		boolean occured = false;
		for (int i = 0; i < MAX_CARDS; i++) {
			int currentCard = cardWithNoAssert(pkTrick, i);
			if (occured && PackedCard.isValid(currentCard))
				return false;
			if (!PackedCard.isValid(currentCard))
				occured = true;
			if (!PackedCard.isValid(currentCard) && currentCard != PackedCard.INVALID)
				return false;
		}
		return true;
	}

	/**
	 * Get the first packed trick, ie all bits off except trump and player
	 * 
	 * @param trump       (Color)
	 * @param firstPlayer (PlayerId)
	 * @return (int) : first empty packed trick
	 */
	public static int firstEmpty(Color trump, PlayerId firstPlayer) {
		return Bits32.pack(PackedCard.INVALID, SIZE_CARD, PackedCard.INVALID, SIZE_CARD, PackedCard.INVALID, SIZE_CARD,
				PackedCard.INVALID, SIZE_CARD, 0, SIZE_INDEX, firstPlayer.ordinal(), SIZE_PLAYER, trump.ordinal(),
				SIZE_TRUMP);
	}

	/**
	 * Get the next packed trick, ie index (of trick in the turn) increase of 1, and
	 * first player is replaced by the winner of the given packed trick
	 * 
	 * @param pkTrick (int) : old packed trick
	 * @return (int) : next packed trick
	 */
	public static int nextEmpty(int pkTrick) {
		assert isValid(pkTrick);
		if (!isLast(pkTrick)) {
			Color color = trump(pkTrick);
			PlayerId player = winningPlayer(pkTrick);
			return firstEmpty(color, player) | (index(pkTrick) + 1 << START_INDEX);
		} else {
			return INVALID;
		}
	}

	/**
	 * Check if it is the last trick in the turn
	 * 
	 * @param pkTrick (int) : the current packed trick
	 * @return (boolean) : true iff it's the last trick
	 */
	public static boolean isLast(int pkTrick) {
		return index(pkTrick) == (Jass.TRICKS_PER_TURN - 1);
	}

	/**
	 * Check if the given packed trick is empty, ie it contains 0 cards
	 * 
	 * @param pkTrick (int)
	 * @return (boolean) : true iff pkTrick is empty
	 */
	public static boolean isEmpty(int pkTrick) {
		return size(pkTrick) == 0;
	}

	/**
	 * Check if the given packed trick is full, ie it contains 4 cards
	 * 
	 * @param pkTrick (int)
	 * @return (boolean) : true iff the packed trick is full
	 */
	public static boolean isFull(int pkTrick) {
		return size(pkTrick) == MAX_CARDS;
	}

	/**
	 * Get the number of played cards in the given packed trick
	 * 
	 * @param pkTrick (int)
	 * @return (int) : number of cards in the packed trick
	 */
	public static int size(int pkTrick) {
		assert isValid(pkTrick);
		int count = 0;
		for (int i = 0; i < MAX_CARDS; i++) {
			if (card(pkTrick, i) != PackedCard.INVALID)
				count++;
		}
		return count;
	}

	/**
	 * Get trump color from the given packed trick
	 * 
	 * @param pkTrick (int)
	 * @return (Color) : trump color of the packed trick
	 */
	public static Color trump(int pkTrick) {
		assert isValid(pkTrick);
		return Color.ALL.get(extract(pkTrick, START_TRUMP, SIZE_TRUMP));
	}

	/**
	 * Get the player at a given index in the packed trick, index 0 is the first
	 * player of the trick
	 * 
	 * @param pkTrick (int)
	 * @param index   (int)
	 * @return (PlayerId) : the player at the given index of the packed trick
	 */
	public static PlayerId player(int pkTrick, int index) {
		assert isValid(pkTrick);
		assert (index >= 0 && index < MAX_CARDS);
		return PlayerId.ALL.get((extract(pkTrick, START_PLAYER, SIZE_PLAYER) + index) % PlayerId.COUNT);
	}

	/**
	 * Get index value of the packed trick
	 * 
	 * @param pkTrick (int)
	 * @return (int) : index
	 */
	public static int index(int pkTrick) {
		assert isValid(pkTrick);
		return extract(pkTrick, START_INDEX, SIZE_INDEX);
	}

	/**
	 * Get the packed card at a given index in the packed trick
	 * 
	 * @param pkTrick (int)
	 * @param index   (int)
	 * @return (int) : packed card at a given index of the packed trick
	 */
	public static int card(int pkTrick, int index) {
		assert isValid(pkTrick);
		assert index >= 0 && index < MAX_CARDS;
		return cardWithNoAssert(pkTrick, index);
	}

	/**
	 * Get the packed card at a given index in the packed trick with no assert
	 * 
	 * @param pkTrick (int)
	 * @param index   (int)
	 * @return (int) : packed card at a given index of the packed trick
	 */
	private static int cardWithNoAssert(int pkTrick, int index) {
		return extract(pkTrick, index * SIZE_CARD, SIZE_CARD);
	}

	/**
	 * Add the given packed card to the packed trick
	 * 
	 * @param pkTrick (int)
	 * @param pkCard  (int)
	 * @return (int) : new packed trick with an added card
	 */
	public static int withAddedCard(int pkTrick, int pkCard) {
		assert isValid(pkTrick);
		assert PackedCard.isValid(pkCard);
		int mask = Bits32.mask((size(pkTrick) * SIZE_CARD), SIZE_CARD);
		return pkTrick & ~mask | (pkCard << (size(pkTrick) * SIZE_CARD));
	}

	/**
	 * Get the base color of the trick, ie color of the first played card
	 * 
	 * @param pkTrick (int)
	 * @return (Color) : base color of the packed trick
	 */
	public static Color baseColor(int pkTrick) {
		return PackedCard.color(card(pkTrick, 0));
	}

	/**
	 * Compute a packed cardset of cards from the given hand that can be played next
	 * in the packed trick (supposed not full)
	 * 
	 * @param pkTrick (int)
	 * @param pkHand  (long)
	 * @return (long) : packed cardset of playable cards
	 */
	public static long playableCards(int pkTrick, long pkHand) {
		assert isValid(pkTrick);
		assert PackedCardSet.isValid(pkHand);

		if (size(pkTrick) == 0)
			return pkHand;

		Color baseColor = baseColor(pkTrick);
		Color trump = trump(pkTrick);
		long pkHandTrump = PackedCardSet.subsetOfColor(pkHand, trump);
		long pkHandNoSmallTrump = pkHandTrump;
		long pkHandNoSmallTrumpTemp = pkHandTrump;

		// the set of the played cards
		long trickSet = EMPTY;
		for (int i = 0; i < size(pkTrick); ++i)
			trickSet = add(trickSet, card(pkTrick, i));

		if (baseColor == trump) {
			if (pkHandTrump != 0 && pkHandTrump != singleton(Card.of(trump, Rank.JACK).packed()))
				return pkHandTrump;
			else
				return pkHand;
		} else {
			// Hand without trump smaller than the one(s) pontentially already played
			// for each trump card of the player tests if a better trump card is already
			// played
			for (int i = 0; i < PackedCardSet.size(pkHandTrump); ++i) {
				if (intersection(trumpAbove(get(pkHandTrump, i)), trickSet) != 0)
					pkHandNoSmallTrumpTemp = remove(pkHandNoSmallTrumpTemp, get(pkHandTrump, i));
			}
			// If the player has other card(s) than trump cards or a trump bigger than the
			// one(s) played
			// set trump card(s) with the trump card(s) bigger than the one(s) played
			if (difference(pkHand, pkHandTrump) != 0 || pkHandNoSmallTrumpTemp > 0)
				pkHandNoSmallTrump = pkHandNoSmallTrumpTemp;
			if (subsetOfColor(pkHand, baseColor) != 0)
				return union(pkHandNoSmallTrump, subsetOfColor(pkHand, baseColor(pkTrick)));
			else
				return difference(pkHand, subsetOfColor(complement(pkHandNoSmallTrump), trump));
		}
	}

	/**
	 * Get the value of the given packed trick (points)
	 * 
	 * @param pkTrick (int)
	 * @return (int) : value of the packed trick
	 */
	public static int points(int pkTrick) {
		int points = 0;
		for (int index = 0; index < size(pkTrick); index++)
			points += PackedCard.points(trump(pkTrick), card(pkTrick, index));
		return isLast(pkTrick) ? points + LAST_TRICK_ADDITIONAL_POINTS : points;
	}

	/**
	 * Get the winning player of the given packed trick
	 * 
	 * @param pkTrick (int)
	 * @return (PlayerId) : winning player
	 */
	public static PlayerId winningPlayer(int pkTrick) {
		int winningIndex = 0;
		for (int index = 0; index < size(pkTrick); index++) {
			if (PackedCard.isBetter(trump(pkTrick), card(pkTrick, index), card(pkTrick, winningIndex)))
				winningIndex = index;
		}
		return player(pkTrick, winningIndex);
	}

	/**
	 * Overload toString
	 * 
	 * @param pkTrick (int)
	 * @return (String) : textual representation of a trick
	 */
	public static String toString(int pkTrick) {
		assert isValid(pkTrick);
		StringJoiner j = new StringJoiner(",", "{", "}");
		for (int i = 0; i < size(pkTrick); i++)
			j.add(PackedCard.toString(card(pkTrick, i)));
		return j.toString();
	}
}