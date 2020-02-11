package javass3.gui.beans;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import javass3.jass.Jass;
import javass3.jass.components.Card;
import javass3.jass.components.CardSet;

public final class HandBean {
	
	private final ObservableList<Card> hand;
	private final ObservableSet<Card> playableCards;
	
	// bonus : need help button properties 
	
	private final ObservableList<Card> bestCard;
	private final BooleanProperty mctsHelp;

	/**
	 * Public constructor for the hand bean
	 */
	public HandBean() {
		this.hand = FXCollections.observableArrayList();
		this.playableCards = FXCollections.observableSet();
		this.bestCard = FXCollections.observableArrayList();
		this.mctsHelp = new SimpleBooleanProperty(false);

		for (int i = 0; i < Jass.HAND_SIZE; ++i)
			hand.add(null);
	}

	/**
	 * Get the hand property of the hand bean
	 * 
	 * @return (ObservableList<Card>) : the hand property
	 */
	public ObservableList<Card> hand() {
		return FXCollections.unmodifiableObservableList(hand);
	}

	/**
	 * Set the hand property of the hand bean
	 * 
	 * @param newHand (CardSet) : the cardset we want to set
	 */
	public void setHand(CardSet newHand) {
		if (newHand.size() == Jass.HAND_SIZE) {
			for (int i = 0; i < Jass.HAND_SIZE; ++i)
				hand.set(i, newHand.get(i));
		} else {
			for (Card c : hand) {
				if (c != null && !newHand.contains(c))
					hand.set(hand.indexOf(c), null);
			}
		}
	}

	/**
	 * Get the playable cards property of the hand bean
	 * 
	 * @return (ObservableSet<Card>) : the playable cards property
	 */
	public ObservableSet<Card> playableCards() {
		return FXCollections.unmodifiableObservableSet(playableCards);
	}

	/**
	 * Set the playable cards property of the hand bean
	 * 
	 * @param newPlayableCards (CardSet) : the cardset we want to set
	 */
	public void setPlayableCards(CardSet newPlayableCards) {
		playableCards.clear();
		for (int i = 0; i < newPlayableCards.size(); ++i) {
			playableCards.add(newPlayableCards.get(i));
		}
	}

	// bonus : need help button functions
	
	/**
	 * Get a list containing the best playable card of the hand bean
	 * 
	 * @return (ObservableList<Card>) : the best card property
	 */
	public ObservableList<Card> bestCard() {
		return FXCollections.unmodifiableObservableList(bestCard);
	}

	/**
	 * Set the best card property of the hand bean
	 * 
	 * @param newBestCard (Card) : the card we want to set
	 */
	public void setBestCard(Card newBestCard) {
		bestCard.clear();
		bestCard.add(newBestCard);
	}
	
	/**
	 * Get the boolean property when player ask for Monte Carlo Tree Search help
	 * 
	 * @return (SimpleBooleanProperty) : help property
	 */
	public ReadOnlyBooleanProperty help() {
		return mctsHelp;
	}

	/**
	 * Set the help property of the hand bean
	 * 
	 * @param newValue (Boolean) : the value we want to set
	 */
	public void setHelp(boolean newValue) {
		mctsHelp.set(newValue);
	}
}
