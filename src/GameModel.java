/**
 * 'Simulation' of a connect 4 game
 * contains the state of the game as well as 
 * methods to advance gameplay.
 * (but doesn't deal with the user directly)
 *
 */
public class GameModel {
	private Grid grid;
	private int currentPlayer; // player who is about to move: 1 or 2
	private int lastMoveCol; // 0-based index of last move
	private int count;


	public GameModel () {
		grid = new Grid(6, 7);
		currentPlayer = 1;
		lastMoveCol = -1;
		count = 0;
	}
	
	
	/**
	 * Essentially the inverse of toString
	 * reads the GameModel from a string representation
	 * @param s string representation of a GameModel
	 */
	public void loadFromString(String s) {
		grid = new Grid(6, 7);
		String[] lines = s.split("\n");
		assert(lines.length == 6);
		for (int row = 5; row >= 0; row--) {
			System.out.println("row = "+row);
			String line = lines[5 - row];
			for (int col = 0; col < 7; col++) {
				char c = line.charAt(col);
				
				switch (c) {
				case 'X':
					lastMoveCol = col;
					currentPlayer = 2;
					grid.setCell(col,row, 1);
					break;
				case 'O':
					lastMoveCol = col;
					currentPlayer = 1;
					grid.setCell(col,row, 2);
					break;
				case 'x':
					grid.setCell(col,row, 1);
					break;
				case 'o':
					grid.setCell(col,row, 2);
					break;
				}
			}
		}
	}
	
	/**
	 * Copy constructor.
	 * @param model
	 */
	public GameModel (GameModel model) {
		grid = new Grid(model.grid);
		currentPlayer = model.currentPlayer;
		lastMoveCol = model.lastMoveCol;
	}
	
	/**
	 * Start a fresh game
	 */
	public void restartGame(){
		this.grid = new Grid(6, 7);
		this.currentPlayer = 1;
		this.lastMoveCol = -1;
	}
	
	/**
	 * Get Grid object containing the state of the board
	 * @return
	 */
	public Grid getGrid() {
		return grid;
	}

	/**
	 * Get the player who is next to make a turn
	 * can be 1 or 2.
	 * @return integer id of player
	 */
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Get the column which was most recently played in.
	 * Returns -1 if no-one has played yet.
	 * @return column
	 */
	public int getLastMoveCol() {
		return lastMoveCol;
	}
	
    public int getCount() {
   	 	return count;
    }
	
	/**
	 * Make currentPlayer's move in given column.
	 * This is assumed to be a valid move.
	 * @param col column to move in
	 */
	public void makeMove(int col) {
		lastMoveCol = col;
		
		int row = getFirstEmpty(col);
		if(checkMove(col)){
			grid.setCell(col,row, currentPlayer);
			currentPlayer = getLastPlayer();
		}
	}
	
	/**
	 * Checks if a given move is valid
	 * @param col
	 * @return
	 */
	public boolean checkMove(int col){
		int row = getFirstEmpty(col);
		if(row>5){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Get the player who just went
	 */
	public int getLastPlayer() {
		return (currentPlayer)%2 + 1;
	}
	
	/**
	 * checks if the last move won the game
	 * @return
	 */
	public boolean didLastMoveWin() {
		// Darwin
		boolean result = false;
		if (lastMoveCol == -1) {
			return result;
		}
		int lastMoveRow = getFirstEmpty(lastMoveCol) - 1;

		int gradient;
		
		int lastPlayer = getLastPlayer();

		for (gradient = -2; gradient <= 1; gradient++) {
			// treat gradient = -2 as infinity (vertical line)

			int row, col;
			int count = 0;

			// check the seven spaces around the most recent move:
			int i; // number of spaces checked
			if (gradient == -2) {
				col = lastMoveCol;
				row = lastMoveRow - 3;
			} else {
				col = lastMoveCol - 3;
				row = lastMoveRow - 3*gradient;
			}

			for (i = 0; i <= 7; i++) {
				if (grid.contains(col, row)) {
					if (grid.getCell(col,row) == lastPlayer) {
						count++;
						if (count >= 4) {
							result = true;
							break;
						}
					} else {
						count = 0;
					}
				}

				if (gradient == -2) {
					row++;
				} else {
					col++;
					row += gradient;
				}
			}

		}
		return result;
	}
	
	/**
	 * checks if the last move caused a draw
	 * @return
	 */
	public boolean didLastMoveDraw() {
		boolean result = true;
		for (int col = 0; col < 7; col++) {
			if (isMoveValid(col)) {
				return false;
			}
		}
		return result;
	}
	
	/**
	 * Checks that a given move can be made (that the column is not full)
	 * @param col
	 * @return
	 */
	public boolean isMoveValid(int col) {
		return (getFirstEmpty(col) < 6);
	}
	
	/**
	 * Gets the index of the first (top) empty row in the specified column
	 * on the board
	 * @param col
	 * @return
	 */
	public int getFirstEmpty(int col) {
		for (int row = 0; row < 6; row++) {
			if (grid.getCell(col, row) == 0) {
				return row;
			}
		}
		return 6;
	}
    
    public boolean makeAnimation(int counter) {
    	count = counter;
    	return true;
    }	
	
	
	/**
	 * Returns a string representation of the game
	 * Just a printout of the board as Os and Xs.
	 */
    public String toString() {
		//Todo: Sam
		int row;
		int col;
		char piece;
		
		String s = "";

		
		int lastMoveRow = -1;
		if (lastMoveCol != -1){
			lastMoveRow = getFirstEmpty(lastMoveCol) - 1;
		}
		
		for (row = 5; row >= 0; row--) {
			for (col = 0; col < 7; col++) {
				if (grid.getCell(col, row) != 0) {
					piece = (grid.getCell(col, row) == 1) ? 'x':'o';
					if (col == lastMoveCol && row == lastMoveRow) {
						piece = Character.toUpperCase(piece);
					}
					s += piece;
				} else {
					s += " ";
				}
			}
			if (row != 0) {
				s += "\n";
			}
		}
		return s;
    }
	
	/**
	 * Print the board to stdout
	 */
	public void printBoard() {
		System.out.println(toString());
		System.out.println("1234567");

	}
}
