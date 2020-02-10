package javass3.jass.ids;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TeamId {
	
	TEAM_1, TEAM_2;

	public static final List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int COUNT = TeamId.ALL.size();

	/**
	 * Get the opposite team of current one
	 * 
	 * @return (TeamId) : other team
	 */
	public TeamId other() {
		return this.equals(TEAM_1) ? TEAM_2 : TEAM_1;
	}
}