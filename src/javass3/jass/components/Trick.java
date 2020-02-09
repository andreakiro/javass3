package javass3.jass.components;

import static javass3.Preconditions.checkArgument;
import static javass3.Preconditions.checkIndex;

import javass3.jass.PlayerId;
import javass3.jass.components.Card.Color;
import javass3.jass.packed.PackedTrick;

public class Trick {

	public static final Trick INVALID = new Trick(PackedTrick.INVALID);
	private final int pkTrick;

	private Trick(int pkTrick) {
		this.pkTrick = pkTrick;
	}

	/**
	 * Get the first empty trick
	 * 
	 * @param trump       (Color)
	 * @param firstPlayer (PlayerId)
	 * @return (Trick) : first empty trick
	 */
	public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
		return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
	}

	/**
	 * Get the trick of his given packed version trick
	 * 
	 * @param packed (int) : packed version of the trick
	 * @return (Trick) : new trick
	 * @throws IllegalArgumentException
	 */
	public static Trick ofPacked(int packed) {
		checkArgument(PackedTrick.isValid(packed));
		return new Trick(packed);
	}

	/**
	 * Get the packed version of the trick
	 * 
	 * @return (int) : packed trick
	 */
	public int packed() {
		return pkTrick;
	}

	/**
	 * Get the next trick
	 * 
	 * @throws IllegalStateException if the trick is not full
	 * @return (Trick) : next trick
	 */
	public Trick nextEmpty() {
		checkCondition(PackedTrick.isFull(pkTrick));
		return new Trick(PackedTrick.nextEmpty(pkTrick));
	}

	/**
	 * Check if the trick is empty
	 * 
	 * @return (boolean) : true iff the trick is empty
	 */
	public boolean isEmpty() {
		return PackedTrick.isEmpty(pkTrick);
	}

	/**
	 * Check if the trick is full
	 * 
	 * @return (boolean) : true iff the trick is full
	 */
	public boolean isFull() {
		return PackedTrick.isFull(pkTrick);
	}

	/**
	 * Check if it is the last trick
	 * 
	 * @return (boolean) : true iff it is the last trick
	 */
	public boolean isLast() {
		return PackedTrick.isLast(pkTrick);
	}

	/**
	 * Get the number of played cards in the trick
	 * 
	 * @return (int) : number of played cards in the trick
	 */
	public int size() {
		return PackedTrick.size(pkTrick);
	}

	/**
	 * Get the trump color of the trick
	 * 
	 * @return (Color) : trump color of the trick
	 */
	public Color trump() {
		return PackedTrick.trump(pkTrick);
	}

	/**
	 * Get the index of the trick
	 * 
	 * @return (int) : index of the trick
	 */
	public int index() {
		return PackedTrick.index(pkTrick);
	}

	/**
	 * Get the player at a given index in the trick, or raises an exception if index
	 * is not valid
	 * 
	 * @param index (int)
	 * @throws IndexOutOfBoundsException
	 * @return (PlayerId) : player at a given index in the trick
	 */
	public PlayerId player(int index) {
		checkIndex(index, PlayerId.COUNT);
		return PackedTrick.player(pkTrick, index);
	}

	/**
	 * Get the card at a given index in the trick, or raises an exception if index
	 * is not valid
	 * 
	 * @param index (int)
	 * @throws IndexOutOfBoundsException
	 * @return (Card) : card at a given index of the trick
	 */
	public Card card(int index) {
		checkIndex(index, PackedTrick.size(pkTrick));
		return Card.ofPacked(PackedTrick.card(pkTrick, index));
	}

	/**
	 * Add the given card to the trick
	 * 
	 * @param c (Card)
	 * @throws IllegalStateException if the trick is full
	 * @return (Trick) : new trick with an added card
	 */
	public Trick withAddedCard(Card c) {
		checkCondition(!PackedTrick.isFull(pkTrick));
		return new Trick(PackedTrick.withAddedCard(pkTrick, c.packed()));
	}

	/**
	 * Get the base color of the trick
	 * 
	 * @throws IllegalStateException if the trick is empty
	 * @return (Color) : base color of the trick
	 */
	public Color baseColor() {
		checkCondition(!PackedTrick.isEmpty(pkTrick));
		return PackedTrick.baseColor(pkTrick);
	}

	/**
	 * Compute a cardset containing cards from the given hand that can be played
	 * 
	 * @param hand (CardSet)
	 * @throws IllegalStateException if the trick is full
	 * @return (CardSet) : cardset of playable cards
	 */
	public CardSet playableCards(CardSet hand) {
		checkCondition(!PackedTrick.isFull(pkTrick));
		return CardSet.ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
	}

	/**
	 * Get the value of the trick (points)
	 * 
	 * @return (int) : value of the trick
	 */
	public int points() {
		return PackedTrick.points(pkTrick);
	}

	/**
	 * Get the winning player of the trick
	 * 
	 * @throws IllegalStateException if the trick is empty
	 * @return (PlayerId) : winning player in the trick
	 */
	public PlayerId winningPlayer() {
		checkCondition(!PackedTrick.isEmpty(pkTrick));
		return PackedTrick.winningPlayer(pkTrick);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		else if (arg0.getClass() != getClass())
			return false;
		else
			return pkTrick == ((Trick) arg0).packed();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pkTrick;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PackedTrick.toString(pkTrick);
	}
	
	private void checkCondition(boolean b) {
		if (!b)
			throw new IllegalStateException();
	}
}