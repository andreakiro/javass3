package javass3.jass.components;

import static javass3.jass.packed.PackedCard.isValid;
import static javass3.jass.packed.PackedCard.pack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javass3.jass.packed.PackedCard;

public class Card {
	
	private final int packedCard;

	private Card(int packedCard) {
		this.packedCard = packedCard;
	}

	/**
	 * Get the card of given color and rank
	 * 
	 * @param c (Color)
	 * @param r (Rank)
	 * @return (Card) : card of given color and rank
	 */
	public static Card of(Color c, Rank r) {
		int card = pack(c, r);
		return new Card(card);
	}

	/**
	 * Get the card corresponding to the given int (the packed value)
	 * 
	 * @param packed (int)
	 * @return (Card) : the card which the given int is the packed value
	 */
	public static Card ofPacked(int packed) {
		assert isValid(packed);
		return new Card(packed);
	}

	/**
	 * Get the packed version of the card
	 * 
	 * @return (int) : the packed card
	 */
	public int packed() {
		return packedCard;
	}

	/**
	 * Get the color of the card
	 * 
	 * @return (Color) : color of the card
	 */
	public Color color() {
		return PackedCard.color(packedCard);
	}

	/**
	 * Get the card rank
	 * 
	 * @return (Rank) : rank of the card
	 */
	public Rank rank() {
		return PackedCard.rank(packedCard);
	}

	/**
	 * Check if the current card (this) is better then the other card (that)
	 * 
	 * @param trump (Color) : if trump the cards ranking changes
	 * @param that  (Card) : the other card
	 * @return (boolean) : true iff the current card (this) is better then the other
	 *         card (that)
	 */
	public boolean isBetter(Color trump, Card that) {
		return PackedCard.isBetter(trump, packedCard, that.packedCard);
	}

	/**
	 * Get the point value of the card
	 * 
	 * @param trump (Color) : if trump the cards ranking changes
	 * @return (int) : the card value
	 */
	public int points(Color trump) {
		return PackedCard.points(trump, packedCard);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object thatO) {
		if (thatO == null)
			return false;
		else if (thatO.getClass() != getClass())
			return false;
		else
			return this.packedCard == ((Card) thatO).packedCard;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PackedCard.toString(packedCard);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return packedCard;
	}

	/**
	 * Represent the color of the cards
	 */
	public enum Color {

		SPADE("\u2660"), HEART("\u2665"), DIAMOND("\u2666"), CLUB("\u2663");

		private final String type;

		private Color(String type) {
			this.type = type;
		}

		public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));
		public static final int COUNT = Color.ALL.size();

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return type;
		}
	}

	/**
	 * Represent the rank of the cards
	 */
	public enum Rank {

		SIX(0, "6"), SEVEN(1, "7"), EIGHT(2, "8"), NINE(7, "9"), TEN(3, "10"), JACK(8, "J"), QUEEN(4, "Q"),
		KING(5, "K"), ACE(6, "A");

		private final int trump;
		private final String representation;

		private Rank(int trump, String representation) {
			this.trump = trump;
			this.representation = representation;
		}

		public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(values()));
		public static final int COUNT = Rank.ALL.size();

		/**
		 * Get trump ordinal
		 * 
		 * @return (int) : the position in enum of the trump card
		 */
		public int trumpOrdinal() {
			return trump;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return representation;
		}
	}
}