package javass3.jass.packed;

import static javass3.bits.Bits32.extract;
import static javass3.bits.Bits64.extract;

import javass3.bits.Bits32;
import javass3.bits.Bits64;
import javass3.jass.Jass;
import javass3.jass.ids.TeamId;

public class PackedScore {
	
	private static final int MIN_POINTS = 0;
	private static final int MAX_POINTS_PER_TURN = 257;
	private static final int MAX_POINTS_PER_GAME = Jass.WINNING_POINTS * 2;

	private static final int SIZE_TRICKS = 4;
	private static final int SIZE_TURNS = 9;
	private static final int SIZE_GAME = 11;
	private static final int SIZE_UNUSED_BITS = 8;

	private static final int START_TRICKS = 0;
	private static final int START_TURNS = SIZE_TRICKS;
	private static final int START_GAME = START_TURNS + SIZE_TURNS;
	private static final int START_UNUSED_BITS = START_GAME + SIZE_GAME;
	private static final int START_TEAM_2 = START_UNUSED_BITS + SIZE_UNUSED_BITS;

	public static final long INITIAL = 0l;

	private PackedScore() {
	}

	/**
	 * Check if the given packed score is valid
	 * 
	 * @param pkScore (long)
	 * @return (boolean) : true iff the given score is valid
	 */
	public static boolean isValid(long pkScore) {
		int scoreTeam1 = (int) pkScore;
		int scoreTeam2 = (int) (pkScore >>> START_TEAM_2);
		return isHalfValid(scoreTeam1) && isHalfValid(scoreTeam2);
	}

	/**
	 * Check if the half packed score is valid
	 * 
	 * @param pkScoreTeam (int)
	 * @return (boolean) : true iff half score is valid
	 */
	private static boolean isHalfValid(int pkScoreTeam) {
		int tricks = extract(pkScoreTeam, START_TRICKS, SIZE_TRICKS);
		int turn = extract(pkScoreTeam, START_TURNS, SIZE_TURNS);
		int game = extract(pkScoreTeam, START_GAME, SIZE_GAME);
		int zero = extract(pkScoreTeam, START_UNUSED_BITS, SIZE_UNUSED_BITS);
		return (tricks >= MIN_POINTS) && (tricks <= Jass.TRICKS_PER_TURN) && (turn >= MIN_POINTS)
				&& (turn <= MAX_POINTS_PER_TURN) && (game <= MAX_POINTS_PER_GAME) && (zero == MIN_POINTS);
	}

	/**
	 * Get the packed score given the turn tricks (tricks collected), turn points
	 * and game points of both team
	 * 
	 * @param turnTricks1 (int)
	 * @param turnPoints1 (int)
	 * @param gamePoints1 (int)
	 * @param turnTricks2 (int)
	 * @param turnPoints2 (int)
	 * @param gamePoints2 (int)
	 * @return (long) : packed score
	 */
	public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2,
			int gamePoints2) {
		long t1 = Bits32.pack(turnTricks1, SIZE_TRICKS, turnPoints1, SIZE_TURNS, gamePoints1, SIZE_GAME);
		long t2 = Bits32.pack(turnTricks2, SIZE_TRICKS, turnPoints2, SIZE_TURNS, gamePoints2, SIZE_GAME);
		return t1 | (t2 << START_TEAM_2);
	}

	/**
	 * Get number of won/collected tricks given a team
	 * 
	 * @param pkScore (long)
	 * @param t       (TeamId)
	 * @return (int) : won tricks
	 */
	public static int turnTricks(long pkScore, TeamId t) {
		assert isValid(pkScore);
		int start = t.equals(TeamId.TEAM_1) ? START_TRICKS : START_TRICKS + START_TEAM_2;
		return (int) extract(pkScore, start, SIZE_TRICKS);
	}

	/**
	 * Get turn points given a team
	 * 
	 * @param pkScore (long)
	 * @param t       (TeamId)
	 * @return (int) : turn points
	 */
	public static int turnPoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		int start = t.equals(TeamId.TEAM_1) ? START_TURNS : START_TURNS + START_TEAM_2;
		return (int) extract(pkScore, start, SIZE_TURNS);
	}

	/**
	 * Get game points given a team
	 * 
	 * @param pkScore (long)
	 * @param t       (TeamId)
	 * @return (int) : game points
	 */
	public static int gamePoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		int start = t.equals(TeamId.TEAM_1) ? START_GAME : START_GAME + START_TEAM_2;
		return (int) extract(pkScore, start, SIZE_GAME);
	}

	/**
	 * Get total points given a team and a packed score, ie turn points + game
	 * points
	 * 
	 * @param pkScore (long)
	 * @param t       (TeamId)
	 * @return (int) : total points
	 */
	public static int totalPoints(long pkScore, TeamId t) {
		return gamePoints(pkScore, t) + turnPoints(pkScore, t);
	}

	/**
	 * Update the packed score when a team wins a tricks, if the team just won 9
	 * tricks add a match bonus
	 * 
	 * @param pkScore     (long)
	 * @param winningTeam (TeamId)
	 * @param trickPoints (int)
	 * @return (long) : updated packed score
	 */
	public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
		long tricks = turnTricks(pkScore, winningTeam) + 1;
		long turn = turnPoints(pkScore, winningTeam) + trickPoints;

		if (tricks == Jass.TRICKS_PER_TURN)
			turn += Jass.MATCH_ADDITIONAL_POINTS;

		long t1 = ~Bits64.mask(START_TRICKS, SIZE_TRICKS + SIZE_TURNS) & pkScore | tricks | (turn << START_TURNS);
		long t2 = ~Bits64.mask(START_TRICKS + START_TEAM_2, SIZE_TRICKS + SIZE_TURNS) & pkScore | tricks << START_TEAM_2
				| (turn << START_TURNS + START_TEAM_2);
		return winningTeam.equals(TeamId.TEAM_1) ? t1 : t2;
	}

	/**
	 * Get packed score for next turn
	 * 
	 * @param pkScore (long)
	 * @return (long) : next turn packed score
	 */
	public static long nextTurn(long pkScore) {
		int points1 = totalPoints(pkScore, TeamId.TEAM_1);
		int points2 = totalPoints(pkScore, TeamId.TEAM_2);
		return pack(0, 0, points1, 0, 0, points2);
	}

	/**
	 * Overload of toString
	 * 
	 * @param pkScore (long)
	 * @return (String) : textual representation of a score
	 */
	public static String toString(long pkScore) {
		assert isValid(pkScore);

		StringBuilder sb = new StringBuilder();

		for (TeamId t : TeamId.ALL) {
			if (t == TeamId.TEAM_2)
				sb.append("/");
			sb.append("(").append(turnTricks(pkScore, t)).append(",").append(turnPoints(pkScore, t)).append(",")
					.append(gamePoints(pkScore, t)).append(")");
		}

		return sb.toString();
	}
}