package javass3.gui.beans;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public final class TrumpBean {
	
	private final BooleanProperty isWaiting;
	private final BooleanProperty isChoosing;
	private final BooleanProperty canPass;

	/**
	 * Public constructor for the trump bean
	 */
	public TrumpBean() {
		this.isWaiting = new SimpleBooleanProperty(false);
		this.isChoosing = new SimpleBooleanProperty(false);
		this.canPass = new SimpleBooleanProperty(true);
	}

	/**
	 * Get isWaiting property
	 * 
	 * @return if the player isWaiting
	 */
	public ReadOnlyBooleanProperty isWaitingProperty() {
		return isWaiting;
	}

	/**
	 * Set isWaiting property
	 * 
	 * @param b (boolean)
	 */
	public void setIsWaiting(boolean b) {
		isWaiting.set(b);
	}

	/**
	 * Get boolean property if a player is choosing the trump
	 * 
	 * @return (ReadOnlyBooleanProperty) : isChoosingProperty
	 */
	public ReadOnlyBooleanProperty isChoosingProperty() {
		return isChoosing;
	}

	/**
	 * Set isChoosing property
	 * 
	 * @param b (boolean) : true if a player is choosing the trump
	 */
	public void setIsChoosing(Boolean b) {
		isChoosing.set(b);
	}

	/**
	 * Get the canPassProperty
	 * 
	 * @return (ReadOnlyBooleanProperty) : true if the player canPass
	 */
	public ReadOnlyBooleanProperty canPassProperty() {
		return canPass;
	}

	/**
	 * Set the canPassProperty
	 * 
	 * @param b (boolean) : true if the player can pass
	 */
	public void setCanPass(Boolean b) {
		canPass.set(b);
	}
}
