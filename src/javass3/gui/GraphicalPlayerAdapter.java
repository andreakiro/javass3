package javass3.gui;

import static javafx.application.Platform.runLater;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javass3.gui.beans.HandBean;
import javass3.gui.beans.ScoreBean;
import javass3.gui.beans.TrickBean;
import javass3.gui.beans.TrumpBean;
import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.CardSet;
import javass3.jass.components.Score;
import javass3.jass.components.Trick;
import javass3.jass.components.TurnState;
import javass3.jass.ids.PlayerId;
import javass3.jass.ids.TeamId;
import javass3.jass.players.MctsPlayer;
import javass3.jass.players.Player;

public class GraphicalPlayerAdapter implements Player {
	
	private static final int CAPACITY = 1;

	protected Player mcts;
	private final ArrayBlockingQueue<Card> queue;
	private final ArrayBlockingQueue<Color> trumpQ;
    private final ArrayBlockingQueue<Boolean> passQ;

	private final ScoreBean sb;
	private final TrickBean tb;
	private final HandBean hb;
	private final TrumpBean tpb;

	/**
	 * Constructor for graphical player adapter
	 */
	public GraphicalPlayerAdapter() {
		this.queue = new ArrayBlockingQueue<Card>(CAPACITY);
		this.trumpQ = new ArrayBlockingQueue<Color>(CAPACITY);
        this.passQ = new ArrayBlockingQueue<Boolean>(CAPACITY);
		
		this.sb = new ScoreBean();
		this.tb = new TrickBean();
		this.hb = new HandBean();
		this.tpb = new TrumpBean();
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState, ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		try {
			runLater(() -> { 
				hb.setPlayableCards(state.trick().playableCards(hand));
				// bonus : need help button
				hb.setHelp(false);
				Card bestCard = mcts.cardToPlay(state, hand);
				hb.setBestCard(bestCard);
			});
			return queue.take();
		} catch (InterruptedException ex) {
			throw new Error();
		} finally {
			runLater(() -> {
				hb.setPlayableCards(CardSet.EMPTY);
				hb.setHelp(false);
			});
		}
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId, java.util.Map)
	 */
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		GraphicalPlayer gui = new GraphicalPlayer(ownId, playerNames, sb, tb, hb, tpb, queue, trumpQ, passQ);
		this.mcts = new MctsPlayer(ownId, new Random().nextLong(), 10_000);
		runLater(() -> gui.createStage().show());
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public void updateHand(CardSet newHand) {
		runLater(() -> {
			hb.setHand(newHand);
			hb.setHelp(false);
		});
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
	 */
	@Override
	public void setTrump(Color trump) {
		runLater(() -> tb.setTrump(trump));
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
	 */
	@Override
	public void updateTrick(Trick newTrick) {
		runLater(() -> tb.setTrick(newTrick));
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
	 */
	@Override
	public void updateScore(Score score) {
		TeamId.ALL.forEach(t -> runLater(() -> {
			sb.setTurnPoints(t, score.turnPoints(t));
			sb.setGamePoints(t, score.gamePoints(t));
			sb.setTotalPoints(t, score.totalPoints(t));
		}));
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
	 */
	@Override
	public void setWinningTeam(TeamId winningTeam) {
		runLater(() -> sb.setWinningTeam(winningTeam));
	}
	
	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#pleaseWait(boolean)
	 */
	@Override
	public void pleaseWait(boolean b) {
		runLater(() -> tpb.setIsWaiting(b));
	}

	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#trumpToChoose(ch.epfl.javass.jass.CardSet, boolean)
	 */
	@Override
	public Color trumpToChoose(CardSet hand, boolean canPass) {
		try {
            runLater(() -> {
            	tpb.setIsWaiting(false);
                tpb.setIsChoosing(true);
                tpb.setCanPass(canPass);
            });

            if (passQ.take()) { 
            	runLater(() -> tpb.setIsWaiting(true));
                return null;
            }
            
            return trumpQ.take();
            
        } catch (InterruptedException e) {
            throw new Error();
        } finally {
            runLater(() -> tpb.setIsChoosing(false));
        }
	}
}
