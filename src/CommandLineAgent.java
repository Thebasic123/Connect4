import java.util.Scanner;

/**
 * Agent controlled by a human at the command line
 * For testing only.
 *
 */
public class CommandLineAgent implements Agent {

	private Scanner scanner;
	
	public CommandLineAgent () {
		scanner = new Scanner(System.in);
	}
	
	@Override
	public int decideMove(GameModel game) {
		int r = scanner.nextInt() - 1;
		
		return r;
	}

}
