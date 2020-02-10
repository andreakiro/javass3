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

public class PacedPlayer implements Player {
	
	private final Player underlyingPlayer;
	private final long minTimeInMilli;

	/**
	 * Public constructor
	 * 
	 * @param underlyingPlayer (Player)
	 * @param minTime          (double)
	 */
	public PacedPlayer(Player underlyingPlayer, double minTime) {
		assert minTime > 0;
		this.underlyingPlayer = underlyingPlayer;
		this.minTimeInMilli = (long) (minTime * 1000);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId, java.util.Map)
	 */
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		underlyingPlayer.setPlayers(ownId, playerNames);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public void updateHand(CardSet newHand) {
		underlyingPlayer.updateHand(newHand);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
	 */
	@Override
	public void setTrump(Color trump) {
		underlyingPlayer.setTrump(trump);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
	 */
	@Override
	public void updateTrick(Trick newTrick) {
		underlyingPlayer.updateTrick(newTrick);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
	 */
	@Override
	public void updateScore(Score score) {
		underlyingPlayer.updateScore(score);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
	 */
	@Override
	public void setWinningTeam(TeamId winningTeam) {
		underlyingPlayer.setWinningTeam(winningTeam);
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState, ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		long startTime = System.currentTimeMillis();
		Card card = underlyingPlayer.cardToPlay(state, hand);
		pace(startTime);
		return card;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#trumpToChoose(ch.epfl.javass.jass.CardSet, boolean)
	 */
	@Override
	public Color trumpToChoose(CardSet hand, boolean canPass) {
		long startTime = System.currentTimeMillis();
		Color trump = underlyingPlayer.trumpToChoose(hand, canPass);
		pace(startTime);
		return trump;
	}
	
	/**
	 * Pace the action by freezing the thread
	 * @param startTime (long) : when the pace has started
	 */
	private void pace(long startTime) {
		long current = System.currentTimeMillis();
		long diff = minTimeInMilli - (current - startTime);
		if (diff > 0) {
			try {
				Thread.sleep(diff);
			} catch (InterruptedException e) {
			}
		}
	}
}