package javass3.jass.ids;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PlayerId {
	
	PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

	public static final List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int COUNT = PlayerId.ALL.size();

	/**
	 * Get the team of current player
	 * 
	 * @return (TeamId) : current team
	 */
	public TeamId team() {
		return (this.ordinal() & 1) == 0 ? TeamId.TEAM_1 : TeamId.TEAM_2;
	}
}