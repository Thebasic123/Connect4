import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * he is called BeastAgent because he is a fucken beast
 * @author darwinvickers
 *
 */
public class BeastAgent implements Agent {

	private int me; // player that this Agent is playing
	private int opponent;
	
	private int totalDepth = 2;
	
	private int difficulty;
	
	// use these to initialise the 
	public static final int EASY = 0;
	public static final int MEDIUM = 1;
	public static final int HARD = 2;
	
	/**
	 * Constructs a new beast of the specified difficulty
	 * Options: BeastAgent.EASY, BeastAgent.MEDIUM, BeastAgent.HARD
	 * @param difficulty
	 */
	public BeastAgent(int difficulty) {
		this.difficulty = difficulty;
		switch (difficulty) {
			case EASY:
				totalDepth = 1;
				break;
			case MEDIUM:
				totalDepth = 3;
				break;
			case HARD:
				totalDepth = 4;
				break;
		}
	}
	
	/**
	 * Get the difficulty level of this AI
	 * @return difficulty
	 */
	public int getDifficulty() {
		return difficulty;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BeastAgent) {
			BeastAgent b = (BeastAgent)o;
			return b.difficulty == this.difficulty;
		}
		return false;
	}
	
	// for testing
	/*public static void main (String[] args) {
		GameModel model = new GameModel();
		int[] moves = {3,3,3,3,3,3, 2,1,5,4,4,4,4,2, 2};
		for (int i = 0; i < moves.length; i++) {
			model.makeMove(moves[i]); 
			model.printBoard();
		}
		BeastAgent beast = new BeastAgent(EASY);
		System.out.println("Move: "+beast.decideMove(model));
	}*/
	
	
	public int decideMove(GameModel model) {
		me = model.getCurrentPlayer();
		opponent = model.getLastPlayer();
		
		GameModel bestOption = Collections.max(
			childStates(model),
			new Comparator<GameModel>() {
        		public int compare(GameModel a, GameModel b) {
        			return scoreMinMax(a, totalDepth) - scoreMinMax(b, totalDepth);
        		}
    		}
		);
		
		System.out.println("Scores:"+scoreSet(childStates(model), totalDepth));
		
		
		
		System.out.println("Making move with score:"+scoreMinMax(bestOption, totalDepth));
		
		return bestOption.getLastMoveCol();
	}
	
	private int scoreMinMax(GameModel state, int depth) {
		if (depth == 0) {
			return evaluateState(state, depth);
		}
		
		Set<GameModel> potentialStates = childStates(state);
	    
		if (potentialStates.isEmpty()) {
			return evaluateState(state, depth);
		}
		
		Set<Integer> scores = scoreSet(potentialStates, depth - 1);
		
	    if (state.getCurrentPlayer() == me) {

	    	// my turn
	    	return Collections.max(scores);
	    } else {
	    	// opponent's turn
	    	int x = Collections.min(scores);
	   // 	System.out.println("opponent @ depth = "+depth);
	    	if (depth == 4) {
	    		System.out.println("x = "+x);
	    	}
	    	return x;
	    }
	}
	
	private Set<GameModel> childStates(GameModel state) {
		Set<GameModel> set = new HashSet<GameModel>();
		
		if (state.didLastMoveWin()) {
			return set;
		}
		
		for (int col = 0; col < 7; col++) {
			GameModel potentialState = new GameModel(state);
			
			// skip invalid moves
			if (!potentialState.isMoveValid(col)) {
				continue;
			}
			
			potentialState.makeMove(col);
			set.add(potentialState);
		}
		return set;
	}
	
	// essentially maps the minmax function at a given depth
	// onto the members of the set
	private Set<Integer> scoreSet(Set<GameModel> states, int depth) {
		Set<Integer> scores = new HashSet<Integer>();
		for (GameModel state: states) {
			scores.add(scoreMinMax(state, depth));
		}
		return scores;
	}
	
	
	// the higher the score, the better (for me, not necessarily the 'current player' in this state)
	private int evaluateState(GameModel state, int depth) {
		// right, up, up/right, up/left
		int[][] vectors = {{1,0},{0,1},{1,1},{-1,1}};
		int[][] rowBounds = {{0,5},{0, 2},{0, 2},{0, 2}};
		int[][] colBounds = {{0,3},{0,6},{0,3},{3,6}};
		
		Grid grid = state.getGrid();
		
		// number of sequence[i] = number of (i+1) tokens out of 4 uninterrupted
		int[] mySequenceCounts = {0, 0, 0, 0};
		int[] opponentSequenceCounts = {0, 0, 0, 0};
		
		int v; // index into vectors array
		for (v = 0; v<=3; v++) {

			for (int row = rowBounds[v][0]; row <= rowBounds[v][1]; row++) {
				for (int col = colBounds[v][0]; col <= colBounds[v][1]; col++) {
					int myTokenCount = 0;
					int opponentTokenCount = 0;
					// search 4 spaces along vector from this position
					int searchRow = row;
					int searchCol = col;
					for (int disp = 0; disp < 4; disp++) {						
						int token = grid.getCell(searchCol, searchRow);
						
						if (token == me) {
							myTokenCount++;
							opponentTokenCount = -100; //-inf
						} else if (token == opponent) {
							// opponent's token
							myTokenCount = -100; //-inf
							opponentTokenCount++;
						}

						searchCol += vectors[v][0];
						searchRow += vectors[v][1];
					}
					
					if (myTokenCount > 0) {
						mySequenceCounts[myTokenCount - 1]++;
					}
					if (opponentTokenCount > 0) {
						opponentSequenceCounts[opponentTokenCount - 1]++;
					}
				}
			}
		}
		
		//System.out.println("mySequenceCounts: "+ Arrays.toString(mySequenceCounts));
		//System.out.println("opponentSequenceCounts: "+ Arrays.toString(opponentSequenceCounts));
		int score = 0;
		
		
		if (mySequenceCounts[3] > 0) {
		//	System.out.println("This state:");
		//	state.printBoard();
		//	System.out.println("Results in a win for BeastAgent");
			// I've won the game.
			score = 100000;
		} else if (opponentSequenceCounts[3] > 0) {

		//	System.out.println("This state:");
		//	state.printBoard();
		//	System.out.println("Results in a loss for BeastAgent");
			// Opponent's won the game
			score = -100000;
		} else {
		
			// this weighting is pretty arbitrary
			score = 10*( (
					1*mySequenceCounts[0] +
					3*mySequenceCounts[1] +
					5*mySequenceCounts[2]
				) - (
					1*opponentSequenceCounts[0] +
					3*opponentSequenceCounts[1] +
					5*opponentSequenceCounts[2]
				)
			);
		}
		// depth contributes to score a little 
		// to motivate longer games
		return score + depth;
	}
}
