package javass3.jass.components;

import static javass3.Preconditions.checkArgument;

import javass3.jass.TeamId;
import javass3.jass.packed.PackedScore;

public class Score {
	
	public static final Score INITIAL = new Score(0l);
	private final long packedScore;

	private Score(long packed) {
		packedScore = packed;
	}

	/**
	 * Get the score given his packed version
	 * 
	 * @param packed (long)
	 * @return (Score) : score
	 * @throws IllegalArgumentException
	 */
	public static Score ofPacked(long packed) {
		checkArgument(PackedScore.isValid(packed));
		return new Score(packed);
	}

	/**
	 * Get the packed version of the score
	 * 
	 * @return (long) : packed version score
	 */
	public long packed() {
		return packedScore;
	}

	/**
	 * Get the number of tricks won by the given team
	 * 
	 * @param t (TeamId)
	 * @return (int) : won tricks
	 */
	public int turnTricks(TeamId t) {
		return PackedScore.turnTricks(packedScore, t);
	}

	/**
	 * Get points won during the current turn by the given team
	 * 
	 * @param t (TeamId)
	 * @return (int) : current turn points
	 */
	public int turnPoints(TeamId t) {
		return PackedScore.turnPoints(packedScore, t);
	}

	/**
	 * Get points won during the current game by the given team
	 * 
	 * @param t (TeamId)
	 * @return (int) : current game points
	 */
	public int gamePoints(TeamId t) {
		return PackedScore.gamePoints(packedScore, t);
	}

	/**
	 * Get total points won by the given team, ie game points + current turn points
	 * 
	 * @param t (TeamId)
	 * @return (int) : total points
	 */
	public int totalPoints(TeamId t) {
		return PackedScore.totalPoints(packedScore, t);
	}

	/**
	 * Get score with an additional trick, throws an exception if trick points is
	 * smaller then 0
	 * 
	 * @param winningTeam (TeamId)
	 * @param trickPoints (int)
	 * @return (Score) : new score
	 * @throws IllegalArgumentException
	 */
	public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
		checkArgument(trickPoints >= 0);
		return new Score(PackedScore.withAdditionalTrick(packedScore, winningTeam, trickPoints));
	}

	/**
	 * Get the score for next turn
	 * 
	 * @return (Score) : new score
	 */
	public Score nextTurn() {
		return new Score(PackedScore.nextTurn(packedScore));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object thatO) {
		if (thatO == null)
			return false;
		else if (thatO.getClass() != getClass())
			return false;
		else
			return packedScore == ((Score) thatO).packed();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Long.hashCode(packedScore);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PackedScore.toString(packedScore);
	}
}