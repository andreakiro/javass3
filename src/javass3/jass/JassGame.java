package javass3.jass;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.Card.Rank;
import javass3.jass.components.CardSet;
import javass3.jass.components.Score;
import javass3.jass.components.TurnState;
import javass3.jass.ids.PlayerId;
import javass3.jass.ids.TeamId;
import javass3.jass.players.Player;

public class JassGame {
	
	private static final int SIZE_HAND = Jass.TRICKS_PER_TURN;
	private static final Card FIRST_CARD = Card.of(Color.DIAMOND, Rank.SEVEN);

	private final Random shuffleRng;

	private static Map<PlayerId, Player> players;
	private static Map<PlayerId, String> playerNames;
	private static Map<PlayerId, CardSet> playerCards;
	private static List<Card> deck;

	private PlayerId actualPlayer;
	private static boolean isGameOver;

	private static TurnState turnState;

	/**
	 * Public constructor for JassGame
	 * 
	 * @param rngSeed     (long)
	 * @param players     (Map<PlayerId, Player>)
	 * @param playerNames (Map<PlayerId, String>)
	 */
	@SuppressWarnings("static-access")
	public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
		isGameOver = false;
		Random rng = new Random(rngSeed);
		this.shuffleRng = new Random(rng.nextLong());
		this.players = Collections.unmodifiableMap(new EnumMap<>(players));
		this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
		this.playerCards = new EnumMap<>(PlayerId.class);
		this.deck = new LinkedList<>();
	}

	/**
	 * Check if the game is over
	 * 
	 * @return (boolean) : true iff the game is over
	 */
	public boolean isGameOver() {
		return isGameOver;
	}

	/**
	 * Initialize a turn if it is the first one or if the last one is over. Collect
	 * the trick if is already full. Annouce the turn state to the player. Test if a
	 * team has won. Make the players play until the trick is full
	 */
	public void advanceToEndOfNextTrick() {
		if (!isGameOver) {
			
			if (turnState == null) {
				createNewTurn();
			} else
				turnState = turnState.withTrickCollected();
			checkWinner();
			
			if (isGameOver) {
				updateScore(turnState.score().nextTurn());
				return;
			}

			updateScore(turnState.score());
			setNewTurn();
			updateTrick();

			play();
		}
	}

	/**
	 * Create a new turn if it has never been initialized before, or collect trick
	 */
	private void createNewTurn() {
		// Set for every player, the players
		for (PlayerId p : PlayerId.ALL)
			players.get(p).setPlayers(p, playerNames);
		shuffleDeal();
		actualPlayer = firstPlayer();
		startTurn(Score.INITIAL);
	}

	/**
	 * Set new turn of current one is terminal
	 */
	private void setNewTurn() {
		if (turnState.isTerminal()) {
			actualPlayer = PlayerId.ALL.get((actualPlayer.ordinal() + 1) % PlayerId.COUNT);
			shuffleDeal();
			startTurn(turnState.score().nextTurn());
		}
	}

	/**
	 * Make the players play until the trick is full
	 */
	private void play() {
		while (!turnState.trick().isFull()) {

			// Add the next player's card (he wants to play) to the trick
			PlayerId currentPlayer = turnState.nextPlayer();
			Card playedCard = players.get(currentPlayer).cardToPlay(turnState, playerCards.get(currentPlayer));

			// Update the turnState
			turnState = turnState.withNewCardPlayed(playedCard);

			// Remove the playedCard of the player hand
			playerCards.put(currentPlayer, playerCards.get(currentPlayer).remove(playedCard));

			players.get(currentPlayer).updateHand(playerCards.get(currentPlayer));

			updateTrick();
		}
	}

	/**
	 * Shuffle and deal the deck of cards
	 */
	private void shuffleDeal() {
		// Create a deck with all cards
		for (int i = 0; i < CardSet.ALL_CARDS.size(); ++i)
			deck.add(CardSet.ALL_CARDS.get(i));

		Collections.shuffle(deck, shuffleRng);

		// Create the hand of every player
		for (PlayerId pId : PlayerId.ALL) {
			CardSet cardSet = CardSet.EMPTY;
			for (int i = 0; i < SIZE_HAND; ++i) {
				Card card = deck.get(0);
				cardSet = cardSet.add(card);
				deck.remove(0);
			}
			playerCards.put(pId, cardSet);
			players.get(pId).updateHand(cardSet);
		}
	}

	/**
	 * Start a turn with a random trump, the given score and the actualPlayer
	 * Announce trump color to the players
	 * 
	 * @param score (Score)
	 */
	private void startTurn(Score score) {
		for (Player p : players.values())
			p.pleaseWait(true);
		Color trump = players.get(actualPlayer).trumpToChoose(playerCards.get(actualPlayer), true);
		if (trump == null) {
			PlayerId teamMate = null;
			for (PlayerId pId : PlayerId.ALL) {
				if (pId != actualPlayer && pId.team().equals(actualPlayer.team()))
					teamMate = pId;
			}
			trump = players.get(teamMate).trumpToChoose(playerCards.get(teamMate), false);
		}

		turnState = TurnState.initial(trump, score, actualPlayer);
		for (Player p : players.values())
			p.pleaseWait(false);
		for (Player p : players.values())
			p.setTrump(trump);
	}

	/**
	 * Update the score of each player
	 */
	private void updateScore(Score score) {
		for (Player p : players.values())
			p.updateScore(score);
	}

	/**
	 * Update trick of each player
	 */
	private void updateTrick() {
		for (PlayerId p : PlayerId.ALL)
			players.get(p).updateTrick(turnState.trick());
	}

	/**
	 * Get the first player of the game
	 * 
	 * @return (PLayerId) : first player of the game
	 */
	private PlayerId firstPlayer() {
		for (PlayerId p : PlayerId.ALL)
			if (playerCards.get(p).contains(FIRST_CARD))
				return p;
		return null;
	}

	/**
	 * Check if a team has won, if game is over announce the other players
	 */
	private void checkWinner() {
		for (TeamId t : TeamId.ALL) {
			if (turnState.score().totalPoints(t) >= Jass.WINNING_POINTS) {
				isGameOver = true;
				playerSetWinningTeam(t);
			}
		}
	}

	/**
	 * Announce the winning team to each player
	 * 
	 * @param winningTeam (TeamId)
	 */
	private void playerSetWinningTeam(TeamId winningTeam) {
		for (Player p : players.values())
			p.setWinningTeam(winningTeam);
	}
}
