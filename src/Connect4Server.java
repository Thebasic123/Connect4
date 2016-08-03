
import java.io.*;
import java.net.*;

/**
 * very primitive command-line driven server, for facilitating
 * communication between remote C4 players
 * trust is placed entirely in the clients to send appropriate messages,
 * and clients are responsible for interpreting messages.
 * the server acts only as a relay between clients.
 */

class Connect4Server {
	
	ClientThread[] clients;
	int numClients = 0;
	
	String queuedMessage = null;
	
	int portNumber;
	
	ServerSocket serverSocket;
	
	/**
	 * Construct a new server.
	 * @pre the port is open
	 * @param portNumber The port to serve from.
	 * @throws IOException
	 */
	public Connect4Server(int portNumber) throws IOException {
		this.portNumber = portNumber;
	}
	
	/**
	 * Starts the server serving
	 * @throws IOException
	 */
	public void run() throws IOException{
		System.out.println("new server starting up");
		serverSocket = new ServerSocket(portNumber);
		System.out.println("port granted!");
		
		clients = new ClientThread[2];
		
		while (numClients < 2) {
			System.out.println("waiting...");
			Socket socket = serverSocket.accept();
			System.out.println("new user connected");
			ClientThread t = new ClientThread(numClients, socket);
			clients[numClients] = t;
			numClients++;

			t.start();
		}
	}
	
	/**
	 * Stop server, close the socket
	 */
	public void stop() {

		System.out.println("server.stop() called");
		try {
			System.out.println("closing socket.");
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("couldn't close");
		}
	}
	
	// to be called when client sends a message.
	// simply relays message to other client
	private void receiveMessage(int from, String message) {
		System.out.println("recieved message "+message+"from "+from);
		String out = message+'\n'; //.toUpperCase() + '\n';
		// relay move to other client
		if (numClients == 2) {
			clients[1 - from].sendMessage(out);
		} else {
			queuedMessage = out;
		}
	}
	
	// not used, but sends a message to all players
	private void broadcast(String message) {
		for (int i = 0; i < numClients; i++) {
			clients[i].sendMessage(message);
		}
	}
	
	/**
	 * A thread to deal with a single client
	 *
	 */
	private class ClientThread extends Thread {
		int id;
		
		BufferedReader input;
		DataOutputStream output;

		/**
		 * 
		 * @param id
		 * @param socket
		 * @throws IOException
		 */
		ClientThread(int id, Socket socket) throws IOException {
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new DataOutputStream(socket.getOutputStream());
					
			this.id = id;
		}

		/**
		 * Start the thread waiting for messages
		 */
		public void run() {
			
			sendMessage("Welcome. You are player "+id+"\n");
			String clientSentence;

			if (numClients == 2 && queuedMessage != null) {
				clients[1].sendMessage(queuedMessage);
			}
			while(true) {
				try {
					clientSentence = input.readLine();
				} catch (IOException e) {
					System.out.println(":(");
					break;
				}
				
				receiveMessage(id, clientSentence);
				
			}
		//	close();
		}
		
		/**
		 * Sends a message to the actual connected client
		 * @param message
		 */
		public void sendMessage(String message) {
			System.out.println("Sending '"+message+"' to client #"+id+".");
			try {
				output.writeBytes(message);
			} catch (IOException e) {
				System.out.println("Exception");
			}
		}
		
	}
}