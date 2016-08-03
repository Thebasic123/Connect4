import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.JCheckBoxMenuItem;

/**
 * Our Connect 4 game.
 * 
 * GUIGame is the main class which takes input from the user,
 * and displays the interface.
 * @author Fufu Hu, Yuan (Martin) Ren, Brendan Roy, Darwin Vickers, Sam Wardhaugh
 *
 */
public class GUIGame extends JFrame{
	GameModel model;
	Agent[] players;
	String[] playerIDs;
	
	GameThread gt;
	
	// for online games
	DataOutputStream outToServer = null;
	ServerThread serverThread = null;
	
	private Painting painting ;
	
	/**
	 * The main function.
	 * No arguments needed.
	 * @param args
	 */
	public static void main (String[] args) {
		GUIGame game = new GUIGame();
		game.run();
	}
	
	/**
	 * Constructor for main class.
	 * Only needs to be called once at the beginning of the user session.
	 */
	public GUIGame(){

		setVisible(true);
		
		players = new Agent[2];
		players[0] = new HumanAgent();
		players[1] = new BeastAgent(BeastAgent.EASY);		
		model = new GameModel();
		
		painting = new Painting(model,players);
		makeUI();
		
		System.out.println("gt started");
	}

	private void run() {
		gt = new GameThread();
		gt.start();
	}

	
	private void restartGame(boolean isNetworked, boolean hosting){
		System.out.println("Restart Game");
		model.restartGame();
		painting.repaint();
		gt.stop(); // apparently this is not good, I'll see if I can fix it soon
		
		if (!isNetworked) {
			outToServer = null;
			if (serverThread != null) {
				stopServer();
			}
		} else if (!hosting) {
			if (serverThread != null) {
				stopServer();
			}
		}
		
		
		gt = new GameThread();
		gt.start();
	}
	
	private void restartGame() {
		restartGame(false, false);
	}
	
	private void loadFromFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line;
			
			// first two lines tell us what the agents are
			for (int i = 0; i< 2; i++) {
				line = br.readLine();
				if (line.equals("H")) {
					players[i] = new HumanAgent();
				} else {
					players[i] = new BeastAgent(Integer.parseInt(line));
				}
			}
			
			String s = "";

			line = br.readLine();
			while (line != null) {
				s+=line+"\n";
				line = br.readLine();
			}
			model.loadFromString(s);
			
			gt.stop(); // apparently this is not good, I'll see if I can fix it soon
			
			outToServer = null;
			painting.repaint();
			
			gt = new GameThread();
			gt.start();
		} finally {
			br.close();
		}
	}
	
	private void saveToFile(File file) throws FileNotFoundException, UnsupportedEncodingException {

		PrintWriter writer = new PrintWriter(file, "UTF-8");
		
		System.out.println("save");
		
		for (Agent agent: players) {
			if (agent instanceof HumanAgent) {
				writer.println("H");
			} else if (agent instanceof BeastAgent) {
				writer.println(((BeastAgent)agent).getDifficulty());
			} else {
				// shouldn't happen
				writer.println("X");
			}
		}
		
		writer.print(model);
		
		
		writer.close();
	}
	
	private class GameThread extends Thread {
		public void run() {
			while (true) {
				int player = model.getCurrentPlayer();
				
				System.out.println("Player "+player+ "turn.");
				
				int move = players[player - 1].decideMove(model);
				int row = 0;
				System.out.println("decideMove finished");

				model.makeMove(move);
				
				if (outToServer != null && players[player - 1] instanceof HumanAgent) {
					System.out.println("communicating move to server");
					try {
						outToServer.writeBytes(move + "\n");
					} catch (IOException e) {
						System.out.println("Couldn't communicate move");
					}
				}
				// if in an online game, communicate the move to the server

				painting.repaint();//redraw our board
				if (move != -1) {
					row = model.getFirstEmpty(move) - 1;
					for(int counter = -100; counter <= (500-row*100); counter++) {
						if(model.makeAnimation(counter)) {
							painting.repaint();
							try {                                 //Delay
					            Thread.sleep(2);                 //5 milliseconds delay
					        } catch(InterruptedException ex) {
					            Thread.currentThread().interrupt();
					        }
						}
					}
				}
				//model.printBoard();
				
				if (model.didLastMoveWin()) {
					System.out.println("Player "+player+" wins!");
					JOptionPane.showMessageDialog( painting,"Player "+player+" wins!",
	                        "Winner!!!", JOptionPane.INFORMATION_MESSAGE);
					model.restartGame();
					painting.repaint();
				} else if (model.didLastMoveDraw()) {
					System.out.println("It's a draw.");
					JOptionPane.showMessageDialog( painting,"It is a DRAW!",
	                        "DRAW", JOptionPane.INFORMATION_MESSAGE);
					model.restartGame();
					painting.repaint();
				}
			}
		}
	}
	
	private class ServerThread extends Thread {
		Connect4Server server;
		public void run() {
			try {
				server = new Connect4Server(6789);
				System.out.println("SERVER = "+server);
				server.run();
			} catch (IOException e) {
				
				System.out.println("couldn't run :(");
			}
		}
		
		public void kill() {
			server.stop();
			this.stop();
		}
	}
	
	private void buttonAction(int move){//how to set button actions
		System.out.println("Button action:"+move);
		if(model.checkMove(move)){
			painting.setMouseOver(-1, -1);
			int player = model.getCurrentPlayer();
			if (players[player-1] instanceof HumanAgent) {
				HumanAgent agent = (HumanAgent)players[player-1];
				
				synchronized(agent) {
					agent.buttonAction(move);
				}
			}
		}else{
			JOptionPane.showMessageDialog( painting,"Selected column is full, please try another one!",
                    "Illegal Move", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void makeButtons(){
		for (int col = 0; col <= 6; col++) {
			final int c = col;
			//column1.setBorderPainted(false);
			JButton button = new JButton();
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
					buttonAction(c);
				}
				
			});
			button.addMouseListener(new MouseListener(){
				public void mouseEntered(java.awt.event.MouseEvent evt) {
					painting.setMouseOver(c, model.getFirstEmpty(c));
			    }
				
			    public void mouseExited(java.awt.event.MouseEvent evt) {
			    	painting.setMouseOver(-1, -1);
			    }

				@Override
				public void mouseClicked(MouseEvent e) {
					// Method required for a MouseListener
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// Method required for a MouseListener
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// Method required for a MouseListener
				}
			});

			button.setBounds(col*100, 0, 100, 600);
			button.setOpaque(false);
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			add(button);
		}
	}
	
	// just runs a server in a separate thread
	private void hostRemoteGame() throws IOException {
		if (serverThread != null) {
			stopServer();
		}
		serverThread = new ServerThread();
		serverThread.start();
	//	System.out.println("SERVER:"+server);
	}
	
	private void stopServer() {
		System.out.println("Stopping server thread");
		serverThread.kill();
	}
	
	private void joinRemoteGame(String ip, boolean hosting) throws IOException {
		System.out.println("joinRemoteGame");

		Socket clientSocket = new Socket(ip, 6789);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		System.out.println("Waiting...");
		String welcomeMessage = inFromServer.readLine();
		System.out.println("Welcome message: " + welcomeMessage);
		
		int whichPlayerAmI = Integer.parseInt(welcomeMessage.replaceAll("[\\D]", ""));
		
		players[whichPlayerAmI] = new HumanAgent();
		players[1 - whichPlayerAmI] = new RemoteAgent(inFromServer);

		restartGame(true, hosting);
		
		System.out.println("ending joinRemoteGame");
	}

	private void makeUI(){
		JMenuBar menubar = new JMenuBar();
		JMenu playModes = new JMenu("Play");
		JMenu colour = new JMenu("Token Colours");
		JMenu help = new JMenu("Help");
		JMenu file = new JMenu("Save/Load");
		


		JMenu onlineGame = new JMenu("Online");
		
		JMenu[] AIGame = new JMenu[2]; 
		AIGame[0] = new JMenu("Human vs Computer");
		AIGame[1] = new JMenu("Computer vs Human");
		JMenu player1Colour = new JMenu("Player1");
		JMenu player2Colour = new JMenu("Player2");
		JMenuItem player1Green = new JMenuItem("Green");
		player1Green.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				painting.changePlayer1Color(Color.GREEN);
				painting.repaint();
			}
		});
		JMenuItem player1Red = new JMenuItem("Red");
		player1Red.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				painting.changePlayer1Color(Color.RED);
				painting.repaint();
			}
		});
		JMenuItem player2Yellow = new JMenuItem("Yellow");
		player2Yellow.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				painting.changePlayer2Color(Color.YELLOW);
				painting.repaint();
			}
		});
		JMenuItem player2Blue = new JMenuItem("Blue");
		player2Blue.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				painting.changePlayer2Color(Color.BLUE);
				painting.repaint();
			}
		});
		JMenuItem playersGame = new JMenuItem("Human vs Human");
		playersGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				players[0] = new HumanAgent();
				players[1] = new HumanAgent();
				restartGame();
			}
		});
		
		JMenuItem hostOnlineGame = new JMenuItem("Host");
		hostOnlineGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				try {
					hostRemoteGame();
					joinRemoteGame("localhost", true);
				} catch (IOException e) {
					System.out.println("Hosting failed :(");
				}
			}
		});
		
		JMenuItem joinOnlineGame = new JMenuItem("Join");
		joinOnlineGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				try {
					// pop up dialog box
					String ip = JOptionPane.showInputDialog("Enter the IP of the server you want to join"); 
					joinRemoteGame(ip, false);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null,
						"Could not connect to server",
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
		

		JMenuItem save = new JMenuItem("Save game");
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				if (outToServer != null) {
					JOptionPane.showMessageDialog(null,
						"Can't save online games",
						"Error",
						JOptionPane.ERROR_MESSAGE
					);
				} else {
				    JFileChooser chooser = new JFileChooser();
				    int returnVal = chooser.showSaveDialog(getParent());
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				    	boolean success = true;
				    	try {
							saveToFile(chooser.getSelectedFile());
						} catch (FileNotFoundException e) {
							success = false;
						} catch (UnsupportedEncodingException e) {
							success = false;
						}
				    	if (!success) {
				    		JOptionPane.showMessageDialog(null,
									"An error occured while saving",
									"Error",
									JOptionPane.ERROR_MESSAGE
							);
				    	}
				    }
				}
			}
		});
		JMenuItem load = new JMenuItem("Load game");
		load.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
			    JFileChooser chooser = new JFileChooser();
			    int returnVal = chooser.showOpenDialog(getParent());
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	boolean success = true;
			    	try {
						loadFromFile(chooser.getSelectedFile());
					} catch (FileNotFoundException e) {
						success = false;
					} catch (IOException e) {
						success = false;
					}
			    	if (!success) {
			    		JOptionPane.showMessageDialog(null,
								"An error occured while loading",
								"Error",
								JOptionPane.ERROR_MESSAGE
						);
			    	}
			    }
			}
		});
		
		for (int i = 0; i < 2; i++) {
			final int j = i;
			JMenuItem beginner = new JMenuItem("Beginner");
			beginner.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
					players[j] = new HumanAgent();
					players[1-j] = new BeastAgent(BeastAgent.EASY);
					restartGame();
				}
			});
			JMenuItem intermediate = new JMenuItem("Intermediate");
			intermediate.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
					players[j] = new HumanAgent();
					players[1-j] = new BeastAgent(BeastAgent.MEDIUM);
					restartGame();
				}
			});
			JMenuItem expert = new JMenuItem("Expert");
			expert.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
					players[j] = new HumanAgent();
					players[1-j] = new BeastAgent(BeastAgent.HARD);
					restartGame();
				}
			});
			
			AIGame[i].add(beginner);
			AIGame[i].add(intermediate);
			AIGame[i].add(expert);
		}
		JMenuItem restart = new JMenuItem("Restart");
		restart.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				restartGame();
			}
		});
		JMenuItem rules = new JMenuItem("Rules of Connect 4");
		rules.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event){
				String rulesText = "Connect Four is a two-player connection game in which the players first choose a color"
						+ "\n and then take turns dropping colored discs from the top into a seven-column, "
						+ "\nsix-row vertically suspended grid. The pieces fall straight down, occupying the"
						+ " \nnext available space within the column. The objective of the game is to connect "
						+ "\nfour of one's own discs of the same color next to each other vertically,"
						+ " \nhorizontally, or diagonally before your opponent. Connect Four is a strongly solved game.";
				JOptionPane.showMessageDialog( painting,rulesText,
                        "Rules of Connect 4", JOptionPane.INFORMATION_MESSAGE);
				
			}
		});
		JMenuItem aboutDevelopers = new JMenuItem("About Developers");
		aboutDevelopers.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog( painting,"This program is made by an awesome group!!!",
                        "About Developers", JOptionPane.INFORMATION_MESSAGE);
            }
        });
		
		
		//add actions of buttons before this line
		playModes.add(playersGame);
		file.add(load);
		file.add(save);
		playModes.add(AIGame[0]);
		playModes.add(AIGame[1]);
		playModes.add(onlineGame);
		playModes.add(restart);
		onlineGame.add(hostOnlineGame);
		onlineGame.add(joinOnlineGame);
		player1Colour.add(player1Red);
		player1Colour.add(player1Green);
		player2Colour.add(player2Blue);
		player2Colour.add(player2Yellow);
		colour.add(player1Colour);
		colour.add(player2Colour);
		help.add(rules);
		help.add(aboutDevelopers);
		menubar.add(playModes);
		menubar.add(colour);
		menubar.add(file);
        menubar.add(Box.createHorizontalGlue());
		menubar.add(help);
		setJMenuBar(menubar);
		makeButtons();
		add(painting);
		setTitle("Connect 4");
		//setResizable(false);//forbid users to resize GUI
		setSize(710,765);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

	}
	
	
}
