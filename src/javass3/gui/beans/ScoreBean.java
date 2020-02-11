package javass3.gui.beans;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javass3.jass.ids.TeamId;

public final class ScoreBean {
	
	private final IntegerProperty turnPoints1;
	private final IntegerProperty turnPoints2;

	private final IntegerProperty gamePoints1;
	private final IntegerProperty gamePoints2;

	private final IntegerProperty totalPoints1;
	private final IntegerProperty totalPoints2;

	private final SimpleObjectProperty<TeamId> winningTeam;

	/**
	 * Public constructor for the score bean
	 */
	public ScoreBean() {
		turnPoints1 = new SimpleIntegerProperty();
		turnPoints2 = new SimpleIntegerProperty();

		gamePoints1 = new SimpleIntegerProperty();
		gamePoints2 = new SimpleIntegerProperty();

		totalPoints1 = new SimpleIntegerProperty();
		totalPoints2 = new SimpleIntegerProperty();

		winningTeam = new SimpleObjectProperty<>();
	}

	/**
	 * Get the turn point property of the score bean
	 * 
	 * @param team (TeamId) : the team whose property we want
	 * @return (ReadOnlyIntegerProperty) : turn point property of the given team
	 */
	public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
		return team.equals(TeamId.TEAM_1) ? turnPoints1 : turnPoints2;
	}

	/**
	 * Set the turn point property of the score bean
	 * 
	 * @param team          (TeamId) : the team whose property we want to set
	 * @param newTurnPoints (int) : the value we want to set
	 */
	public void setTurnPoints(TeamId team, int newTurnPoints) {
		(team.equals(TeamId.TEAM_1) ? turnPoints1 : turnPoints2).set(newTurnPoints);
	}

	/**
	 * Get the game point property of the score bean
	 * 
	 * @param team (TeamId) : the team whose property we want
	 * @return (ReadOnlyIntegerProperty) : game point property of the given team
	 */
	public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
		return team.equals(TeamId.TEAM_1) ? gamePoints1 : gamePoints2;
	}

	/**
	 * Set the game point property of the score bean
	 * 
	 * @param team          (TeamId) : the team whose property we want to set
	 * @param newTurnPoints (int) : the value we want to set
	 */
	public void setGamePoints(TeamId team, int newTurnPoints) {
		(team.equals(TeamId.TEAM_1) ? gamePoints1 : gamePoints2).set(newTurnPoints);
	}

	/**
	 * Get the total point property of the score bean
	 * 
	 * @param team (TeamId) : the team whose property we want
	 * @return (ReadOnlyIntegerProperty) : total point property of the given team
	 */
	public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
		return team.equals(TeamId.TEAM_1) ? totalPoints1 : totalPoints2;
	}

	/**
	 * Set the total point property of the score bean
	 * 
	 * @param team          (TeamId) : the team whose property we want to set
	 * @param newTurnPoints (int) : the value we want to set
	 */
	public void setTotalPoints(TeamId team, int newTurnPoints) {
		(team.equals(TeamId.TEAM_1) ? totalPoints1 : totalPoints2).set(newTurnPoints);
	}

	/**
	 * Get the winning team property of the score bean
	 * 
	 * @return (ReadOnlyObjectProperty) : winning team property
	 */
	public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
		return winningTeam;
	}

	/**
	 * Set the winning team property of the score bean
	 * 
	 * @param team (TeamId) : the team we want to set as winner
	 */
	public void setWinningTeam(TeamId team) {
		winningTeam.set(team);
	}
}
