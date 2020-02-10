package javass3.jass.components;

import static javass3.Preconditions.checkArgument;

import javass3.jass.components.Card.Color;
import javass3.jass.ids.PlayerId;
import javass3.jass.packed.PackedCardSet;
import javass3.jass.packed.PackedScore;
import javass3.jass.packed.PackedTrick;

public class TurnState {
	
	private final long pkScore;
	private final long pkUnplayedCards;
	private final int pkTrick;

	private TurnState(long pkScore, long pkUnplayedCards, int pkTrick) {
		this.pkScore = pkScore;
		this.pkUnplayedCards = pkUnplayedCards;
		this.pkTrick = pkTrick;
	}

	/**
	 * Get the initial turnstate with the given trump, score and first player
	 * 
	 * @param trump       (Color) : of the new turn
	 * @param score       (Score) : of the game
	 * @param firstPlayer (PlayerId) : of the new turn
	 * @return (TurnState) : new initial turnstate
	 */
	public static TurnState initial(Color trump, Score score, PlayerId firstPlayer) {
		return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, PackedTrick.firstEmpty(trump, firstPlayer));
	}

	/**
	 * Get turn state of the given packed components
	 * 
	 * @param pkScore         (long)
	 * @param pkUnplayedCards (long)
	 * @param pkTrick         (long)
	 * @return (TurnState) : new turnstate of given components
	 * @throws IllegalArgumentException
	 */
	public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
		checkArgument(PackedScore.isValid(pkScore));
		checkArgument(PackedCardSet.isValid(pkUnplayedCards));
		checkArgument(PackedTrick.isValid(pkTrick));
		return new TurnState(pkScore, pkUnplayedCards, pkTrick);
	}

	/**
	 * Get packed version of current score
	 * 
	 * @return (long) : packed score of the turnstate
	 */
	public long packedScore() {
		return pkScore;
	}

	/**
	 * Get packed version of unplayed cards
	 * 
	 * @return (long) : packed unplayed cards of the turnstate
	 */
	public long packedUnplayedCards() {
		return pkUnplayedCards;
	}

	/**
	 * Get packed version of current trick
	 * 
	 * @return (long) : packed trick of the turnstate
	 */
	public int packedTrick() {
		return pkTrick;
	}

	/**
	 * Get current score
	 * 
	 * @return (Score) : score of the turnstate
	 */
	public Score score() {
		return Score.ofPacked(pkScore);
	}

	/**
	 * Get unplayed cardset
	 * 
	 * @return (CardSet) : cardset of the turnstate
	 */
	public CardSet unplayedCards() {
		return CardSet.ofPacked(pkUnplayedCards);
	}

	/**
	 * Get current trick
	 * 
	 * @return (Trick) : trick of the turnstate
	 */
	public Trick trick() {
		return Trick.ofPacked(pkTrick);
	}

	/**
	 * Check if the state is terminal, ie last trick has been played
	 * 
	 * @return (boolean) : true iff is terminal
	 */
	public boolean isTerminal() {
		return pkTrick == PackedTrick.INVALID;
	}

	/**
	 * Get id of the player to play the next card
	 * 
	 * @return (PlayerId) : next player
	 */
	public PlayerId nextPlayer() {
		checkCondition(!PackedTrick.isFull(pkTrick));
		return PackedTrick.player(pkTrick, PackedTrick.size(pkTrick));
	}

	/**
	 * Get state after the next player has played the given card
	 * 
	 * @param card (Card)
	 * @return (TurnState) : new turnstate with new card played
	 */
	public TurnState withNewCardPlayed(Card card) {
		checkCondition(!PackedTrick.isFull(pkTrick));
		checkCondition(PackedCardSet.contains(pkUnplayedCards, card.packed()));
		return new TurnState(pkScore, PackedCardSet.remove(pkUnplayedCards, card.packed()),
				PackedTrick.withAddedCard(pkTrick, card.packed()));
	}

	/**
	 * Get state after the current trick has been collected
	 * 
	 * @throws IllegalStateException if trick isn't full
	 * @return (TurnState) : new turnstate with trick collected
	 */
	public TurnState withTrickCollected() {
		checkCondition(PackedTrick.isFull(pkTrick));
		return new TurnState(PackedScore.withAdditionalTrick(pkScore, PackedTrick.winningPlayer(pkTrick).team(),
				PackedTrick.points(pkTrick)), pkUnplayedCards, PackedTrick.nextEmpty(pkTrick));
	}

	/**
	 * Get state after next player has played the given card and trick has been
	 * collected (if full)
	 * 
	 * @param card (Card)
	 * @return (Turnstate) : new turnstate with new card played, and trick collected
	 */
	public TurnState withNewCardPlayedAndTrickCollected(Card card) {
		TurnState ts = withNewCardPlayed(card);
		return ts.trick().isFull() ? ts.withTrickCollected() : ts;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pkScore).append(" ");
		sb.append(pkUnplayedCards).append(" ");
		sb.append(pkTrick).append(" ");
		return sb.toString();
	}

	private void checkCondition(boolean b) {
		if (!b)
			throw new IllegalStateException();
	}
}