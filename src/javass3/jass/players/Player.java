package javass3.jass.players;

import java.util.Map;

import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.CardSet;
import javass3.jass.components.Score;
import javass3.jass.components.Trick;
import javass3.jass.components.TurnState;
import javass3.jass.ids.PlayerId;
import javass3.jass.ids.TeamId;

public interface Player {
	
	/**
	 * Get the card the player wants to play given the turn state and his hand
	 * 
	 * @param state (TurnState) : current turn state
	 * @param hand  (CardSet) : hand of the player
	 * @return (Card) : desired card
	 */
	abstract Card cardToPlay(TurnState state, CardSet hand);
	
	/**
	 * Get the trump the player want to choose
	 * 
	 * @param hand (CardSet): hand of the player
	 * @param canPass (boolean) : if the player can pass or not
	 * @return the color or null if the player passed
	 */
	// abstract vraiment ? 
	abstract Color trumpToChoose(CardSet hand, boolean canPass);

	/**
	 * Called once at the beginning of the game to inform the player that he has
	 * ownId identity and that the different players (including him) are named
	 * according to the content of the associative table playerNames
	 * 
	 * @param ownId       (PlayerId)
	 * @param playerNames (Map<PlayerId, String>)
	 */
	default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
	}

	/**
	 * Called each time the hand of the player changes (beginning of turn and when
	 * he plays a card) to inform him of his new hand
	 * 
	 * @param newHand (CardSet)
	 */
	default void updateHand(CardSet newHand) {
	}

	/**
	 * Called each time the trump changes, ie each new turn, to inform the player
	 * 
	 * @param trump (Color)
	 */
	default void setTrump(Color trump) {
	}

	/**
	 * Called each time the trick changes, ie a card is played or trick collected
	 * and replace it by newTrick
	 * 
	 * @param newTrick (Trick)
	 */
	default void updateTrick(Trick newTrick) {
	}

	/**
	 * Called each time the score changes, ie each time a trick is collected
	 * 
	 * @param score (Score)
	 */
	default void updateScore(Score score) {
	}

	/**
	 * Called once when a team wins, ie obtain 1000 points
	 * 
	 * @param winningTeam (TeamId)
	 */
	default void setWinningTeam(TeamId winningTeam) {
	}
	
	/**
	 * Called when the player must wait for another how is choosing the trump
	 * 
	 * @param b (boolean) : true if the player is waiting
	 */
	default void pleaseWait(boolean b) {
	}
}