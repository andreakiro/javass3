package javass3.gui.beans;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import javass3.jass.components.Card;
import javass3.jass.components.Card.Color;
import javass3.jass.components.Trick;
import javass3.jass.ids.PlayerId;

public final class TrickBean {
	
	private final SimpleObjectProperty<Color> trump;
	private final ObservableMap<PlayerId, Card> trick;
    private final ObjectProperty<PlayerId> winningPlayer;

    /**
     * Public constructor for the trick bean
     */
	public TrickBean() {
		this.trump = new SimpleObjectProperty<>();
		this.trick = FXCollections.observableHashMap();
		this.winningPlayer = new SimpleObjectProperty<>();
	}

	/**
	 * Get the trump property of the trick bean
	 * 
	 * @return (ReadOnlyObjectProperty<Color>) : the trump property
	 */
	public ReadOnlyObjectProperty<Color> trumpProperty() {
		return trump;
	}

	/**
	 * Set the trump property of the trick bean
	 * 
	 * @param color (Color) : the color we want to set
	 */
	public void setTrump(Color color) {
		trump.set(color);
	}

	/**
	 * Get the trick property of the trick bean, ie an unmodifiable map containing
	 * every played cards mapped to the corresponding player
	 * 
	 * @return (ObservableMap<PlayerId, Card>) : the trick property
	 */
	public ObservableMap<PlayerId, Card> trickProperty() {
		return FXCollections.unmodifiableObservableMap(trick);
	}
	
	/**
	 * Set the trick property of the trick bean
	 * 
	 * @param newTrick (Trick) : the trick we want to set
	 */
	public void setTrick(Trick newTrick) {
		trick.clear();
		for (int i = 0; i < newTrick.size(); i++)
			trick.put(newTrick.player(i), newTrick.card(i));
		winningPlayer.set(newTrick.isEmpty() ? null : newTrick.winningPlayer());
	}

	/**
	 * Get the winning player property of the trick bean
	 * 
	 * @return (ReadOnlyObjectProperty<PlayerId>) : the winning player property
	 */
	public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
		return winningPlayer;
	}
}
