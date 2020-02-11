package javass3.gui;

import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.CardSet;
import javass3.jass.components.TurnState;

public class GraphicalMctsPlayer extends GraphicalPlayerAdapter {
	
	/* (non-Javadoc)
	 * @see ch.epfl.javass.gui.GraphicalPlayerAdapter#cardToPlay(ch.epfl.javass.jass.TurnState, ch.epfl.javass.jass.CardSet)
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		return mcts.cardToPlay(state, hand);
	}
	
	/* (non-Javadoc)
	 * @see ch.epfl.javass.gui.GraphicalPlayerAdapter#trumpToChoose(ch.epfl.javass.jass.CardSet, boolean)
	 */
	@Override
	public Color trumpToChoose(CardSet hand, boolean canPass) {
		return mcts.trumpToChoose(hand, canPass);
	}
}
