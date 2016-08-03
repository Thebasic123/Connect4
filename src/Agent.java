/**
 * Interface to represent a player of connect 4
 * Including human players (local or remote) as well as
 * robots.
 *
 */
public interface Agent {
	/**
	 * Calculates a move (integer in 0..6) based on the current game state
	 * @return column
	 */
	public int decideMove(GameModel game);
}
