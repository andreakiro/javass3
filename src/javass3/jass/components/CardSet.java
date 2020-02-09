package javass3.jass.components;

import static javass3.Preconditions.checkArgument;
import static javass3.Preconditions.checkIndex;

import java.util.List;

import javass3.jass.components.Card.Color;
import javass3.jass.packed.PackedCardSet;

public class CardSet {
	
	public static final CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);
	public static final CardSet ALL_CARDS = new CardSet(PackedCardSet.ALL_CARDS);
	private final long pkCardSet;

	private CardSet(long pkCardSet) {
		this.pkCardSet = pkCardSet;
	}

	/**
	 * Get the set of cards contained in the given list
	 * 
	 * @param cards (List<Card>)
	 * @return (CardSet) : new cardset
	 */
	public static CardSet of(List<Card> cards) {
		long cardset = PackedCardSet.EMPTY;
		for (Card c : cards)
			cardset = PackedCardSet.add(cardset, c.packed());
		return new CardSet(cardset);
	}

	/**
	 * Get the set of cards given his packed version, throws an exception if packed
	 * version is not valid
	 * 
	 * @param packed (long) : packed cardset
	 * @return (CardSet) : new cardset
	 * @throws IllegalArgumentException
	 */
	public static CardSet ofPacked(long packed) {
		checkArgument(PackedCardSet.isValid(packed));
		return new CardSet(packed);
	}

	/**
	 * Get the packed version of this cardset
	 * 
	 * @return (long) : packed cardset
	 */
	public long packed() {
		return pkCardSet;
	}

	/**
	 * Check if the cardset is empty
	 * 
	 * @return (boolean) : true iff it is empty
	 */
	public boolean isEmpty() {
		return PackedCardSet.isEmpty(pkCardSet);
	}

	/**
	 * Get the number of cards contained in the card set, called size of the set
	 * 
	 * @return (int) : size of the cardset
	 */
	public int size() {
		return PackedCardSet.size(pkCardSet);
	}

	/**
	 * Get a card from the cardset given an index
	 * 
	 * @param index (int)
	 * @throws IndexOutOfBoundsException
	 * @return (Card) : wanted card
	 */
	public Card get(int index) {
		checkIndex(index, size());
		return Card.ofPacked((PackedCardSet.get(pkCardSet, index)));
	}

	/**
	 * Add the given card in the cardset, do nothing if the card is already in the
	 * cardset
	 * 
	 * @param card (Card)
	 * @return (CardSet) : new cardset with an added card
	 */
	public CardSet add(Card card) {
		return new CardSet(PackedCardSet.add(pkCardSet, card.packed()));
	}

	/**
	 * Remove the given card from the cardset, do nothing if the card is not in the
	 * cardset
	 * 
	 * @param card (Card)
	 * @return (CardSet) : new cardset with a removed card
	 */
	public CardSet remove(Card card) {
		return new CardSet(PackedCardSet.remove(pkCardSet, card.packed()));
	}

	/**
	 * Check if the cardset contains the given card
	 * 
	 * @param card (Card)
	 * @return (boolean) : true iff card set contains the card
	 */
	public boolean contains(Card card) {
		return PackedCardSet.contains(pkCardSet, card.packed());
	}

	/**
	 * Get the complement of the cardset
	 * 
	 * @return (CardSet) : new complement cardset
	 */
	public CardSet complement() {
		return new CardSet(PackedCardSet.complement(pkCardSet));
	}

	/**
	 * Get the union of this cardset and the given cardset (that)
	 * 
	 * @param that (CardSet)
	 * @return (CardSet) : new unified cardset
	 */
	public CardSet union(CardSet that) {
		return new CardSet(PackedCardSet.union(pkCardSet, that.packed()));
	}

	/**
	 * Get the intersection of this cardset and the given cardset (that)
	 * 
	 * @param that (CardSet)
	 * @return (CardSet) : new intersected cardset
	 */
	public CardSet intersection(CardSet that) {
		return new CardSet(PackedCardSet.intersection(pkCardSet, that.packed()));
	}

	/**
	 * Get the difference of this cardset and the given cardset (that)
	 * 
	 * @param that (CardSet)
	 * @return (CardSet) : new cardset
	 */
	public CardSet difference(CardSet that) {
		return new CardSet(PackedCardSet.difference(pkCardSet, that.packed()));
	}

	/**
	 * Get a cardset containing all the cards of a color from the given set
	 * 
	 * @param color (Color)
	 * @return (CardSet) : new cardset
	 */
	public CardSet subsetOfColor(Color color) {
		return new CardSet(PackedCardSet.subsetOfColor(pkCardSet, color));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		else if (!(arg0 instanceof CardSet))
			return false;
		else
			return pkCardSet == ((CardSet) arg0).packed();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Long.hashCode(pkCardSet);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PackedCardSet.toString(pkCardSet);
	}
}