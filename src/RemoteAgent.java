import java.io.BufferedReader;
import java.io.IOException;

/**
 * An agent representing a remote player.
 */
public class RemoteAgent implements Agent {

	BufferedReader serverInput; // input from the server
	
	/**
	 * Create a new RemoteAgent,
	 * taking input from the given BufferedReader
	 * @param br a BufferedReader connected to the output of the server
	 */
	public RemoteAgent (BufferedReader br) {
		serverInput = br;
	}
	@Override
	public int decideMove(GameModel game) {
		String sentence;
		System.out.println("RemoteAgent waiting to read line from server");
		try {
			sentence = serverInput.readLine();
		} catch (IOException e) {
			System.out.println("RemoteAgent could not read line");
			return -1;
		}
		System.out.println("Line read.");
		return Integer.parseInt(sentence);
	}

}
