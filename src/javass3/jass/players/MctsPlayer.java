package javass3.jass.players;

import static javass3.Preconditions.checkArgument;
import static java.lang.Math.sqrt;
import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import javass3.jass.Jass;
import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.CardSet;
import javass3.jass.components.Score;
import javass3.jass.components.TurnState;
import javass3.jass.ids.PlayerId;
import javass3.jass.packed.PackedCardSet;
import javass3.jass.packed.PackedTrick;

public class MctsPlayer implements Player {
	
	private final PlayerId own;
	private final SplittableRandom rng;
	private final int iterations;

	private static final int EXPLORATION_NULL = 0;
	private static final int EXPLORATION_FACTOR = 40;
	private static final int MIN_ITERATION = Jass.HAND_SIZE;
	private static final int MIN_POINTS_TRUMP = 16;

	/**
	 * Public constructor
	 * 
	 * @param ownId      (PlayerId)
	 * @param rngSeed    (long)
	 * @param iterations (int)
	 * @throws IllegalArgumentException
	 */
	public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
		checkArgument(iterations >= MIN_ITERATION);
		this.own = ownId;
		this.rng = new SplittableRandom(rngSeed);
		this.iterations = iterations;
	}

	/*
	 * (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState,
	 * ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		Node tree = new Node(state, own, hand.packed());
		computeTree(tree);
		return state.trick().playableCards(hand).get(tree.bestChildIndex(EXPLORATION_NULL));
	}

	/**
	 * Build a MonteCarlo tree
	 * 
	 * @param tree (Node)
	 */
	private void computeTree(Node tree) {
		List<Node> path = new ArrayList<>();
		for (int i = 0; i < iterations; ++i) {
			path.add(tree);
			path = tree.addNewNode(own, path);
			propagation(path);
			path.clear();
		}
	}

	/**
	 * Update the points and turns of all nodes on path
	 * 
	 * @param path (List<Node>)
	 */
	private void propagation(List<Node> path) {
		Score simulatedScore = path.get(path.size() - 1).endOfTurnScore(rng, own);
		for (int i = 0; i < path.size(); ++i) {
			Node node = path.get(i);
			if (i == 0) {
				node.points += simulatedScore.turnPoints(own.team().other());
				node.turns++;
			} else {
				node.points += simulatedScore.turnPoints(path.get(i - 1).current.nextPlayer().team());
				node.turns++;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.epfl.javass.jass.Player#trumpToChoose(ch.epfl.javass.jass.CardSet, boolean)
	 */
	@Override
	public Color trumpToChoose(CardSet hand, boolean canPass) {
		Color bestColor = null;
        int maxPoints = 0;
        for (Color c : Color.ALL) {
            CardSet colorCard = hand.subsetOfColor(c);
            int sum = 0;
            for (int i = 0; i < colorCard.size();++i)
                sum += colorCard.get(i).rank().trumpOrdinal();
            if (maxPoints < sum) {
                bestColor = c;
                maxPoints = sum;
            }
        }           
        return (canPass && maxPoints < MIN_POINTS_TRUMP) ? null : bestColor;
	}

	/**
	 * Represent a node of the tree
	 */
	private final static class Node {

		private final TurnState current;
		private final Node[] children;
		private long hand;
		private int turns;
		private int points;
		private long cardToPlay;

		private Node(TurnState current, PlayerId mctsPlayer, long pkHand) {
			this.current = current;
			this.hand = pkHand;
			this.cardToPlay = potentialCard(current, mctsPlayer, hand);
			this.children = new Node[PackedCardSet.size(cardToPlay)];
			this.turns = 0;
			this.points = 0;
		}

		/**
		 * Create a packed cardset with all the possible playable cards
		 * 
		 * @param turnState (TurnState)
		 * @param mcts      (PlayerId)
		 * @param hand      (long)
		 * @return a PackedCardSet with all possible children
		 */
		private long potentialCard(TurnState ts, PlayerId mcts, long hand) {
			if (!ts.isTerminal()) {
				if (mcts.equals(ts.nextPlayer()))
					return PackedTrick.playableCards(ts.packedTrick(), hand);
				else
					return PackedTrick.playableCards(current.packedTrick(),
							PackedCardSet.difference(ts.packedUnplayedCards(), hand));
			} else {
				return PackedCardSet.EMPTY;
			}
		}

		/**
		 * Add (if possible) a new node on the tree
		 * 
		 * @param mcts (PlayerId)
		 * @param list (List<Node>)
		 * @return (List<Node>) : path from the root to the new node
		 */
		private List<Node> addNewNode(PlayerId mcts, List<Node> list) {
			// Check if every possible child already exists
			if (!PackedCardSet.isEmpty(cardToPlay)) {
				TurnState ts = current;
				int index = children.length - PackedCardSet.size(cardToPlay);
				int card = PackedCardSet.get(cardToPlay, 0);

				ts = ts.withNewCardPlayedAndTrickCollected(Card.ofPacked(card));
				cardToPlay = PackedCardSet.remove(cardToPlay, card);
				children[index] = new Node(ts, mcts, PackedCardSet.remove(hand, card));
				list.add(children[index]);
				return list;
			}

			if (current.isTerminal())
				return list;

			Node bestChild = children[bestChildIndex(EXPLORATION_FACTOR)];
			list.add(bestChild);
			return bestChild.addNewNode(mcts, list);
		}

		/**
		 * Get best child index after computing MonteCarlo tree search
		 * 
		 * @param constant (int)
		 * @return (int) : index of the best child
		 */
		private int bestChildIndex(int constant) {
			double bestChildValue = 0d;
			int index = 0;
			for (int i = 0; i < children.length; i++) {
				double currentChildValue = applyFormula(this, children[i], constant);
				if (bestChildValue < currentChildValue || i == 0) {
					bestChildValue = currentChildValue;
					index = i;
				}
			}
			return index;
		}

		/**
		 * Compute MonteCarlo formula with the given components
		 * 
		 * @param parent   (Node)
		 * @param child    (Node)
		 * @param constant (int)
		 * @return (double) : value of the formula
		 */
		private static double applyFormula(Node parent, Node child, int constant) {
			double p = child.points;
			double t = child.turns;
			double sqrt = sqrt(2d * log(parent.turns) / t);
			return p / t + (constant * sqrt);
		}

		/**
		 * Get the final score of a randomly simulated turn
		 * 
		 * @param rng  (SplittableRandom)
		 * @param mcts (PlayerId)
		 * @return (Score) : final score of a randomly simulated turn
		 */
		private Score endOfTurnScore(SplittableRandom rng, PlayerId mcts) {
			TurnState simulated = current;
			long hand = this.hand;

			// Simulate at random until the end of the turn
			while (!simulated.isTerminal()) {
				int pkCard = randomCard(simulated, hand, rng, mcts);
				simulated = simulated.withNewCardPlayedAndTrickCollected(Card.ofPacked(pkCard));
				hand = PackedCardSet.remove(hand, pkCard);
			}

			return simulated.score();
		}

		/**
		 * Get a random card from possible playable cards
		 * 
		 * @param simulated (TurnState)
		 * @param hand      (long)
		 * @param rng       (SplittableRandom)
		 * @param mcts      (PlayerId)
		 * @return (int) : random packed card
		 */
		private int randomCard(TurnState simulated, long hand, SplittableRandom rng, PlayerId mcts) {
			long playable = potentialCard(simulated, mcts, hand);
			int random = rng.nextInt(PackedCardSet.size(playable));
			return PackedCardSet.get(playable, random);
		}
	}
}