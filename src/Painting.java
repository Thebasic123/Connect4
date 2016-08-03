import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * An extension of JPanel which handles
 * drawing the game board, along with
 * some additional info.
 *
 */
public class Painting extends JPanel  {
	private GameModel model;
	private Agent[] players;
	Color player1Color;
	Color player2Color;
	int mouseCol;
	int mouseRow;
	JLabel infoBoard;
	JLabel turn;
	JLabel gameModel;
	
	/**
	 * Constructs a Painting object
	 * to represent the given GameModel
	 * and players
	 * @param model the GameModel
	 * @param players size-2 array of Agents
	 */
	public Painting(GameModel model,Agent[] players) {
		super();
		this.model = model;
		this.players=players;
		player1Color=Color.RED;//default colors
		player2Color=Color.BLUE;
		infoBoard = new JLabel();
		turn = new JLabel();
		gameModel = new JLabel();
		setMouseOver(-1, -1);

	}
	/**
	 * Set the color of player 1's tokens.
	 * @param c
	 */
	public void changePlayer1Color(Color c) {
		player1Color=c;
	}
	/**
	 * Set the color of player 2's tokens.
	 * @param c
	 */
	public void changePlayer2Color(Color c) {
		player2Color=c;
	}
	
	// draw a single token (circle) at the given position
	private void drawToken(Graphics2D g2d, Color c, int row, int col) {
		int r = 98;
		int x = 100 * col +1;
		int y = 100 * row +1;
		g2d.setColor(Color.BLACK);
		//g2d.fillOval(x, y, r, r);
		r = 90;
		x += 4;
		y += 4;
		g2d.setColor(c);
		g2d.fillOval(x, y, r, r);
	}
	
	/**
	 * Tell the Painting that the mouse is over a specified cell
	 * @param col
	 * @param row
	 */
	public void setMouseOver(int col, int row) {
		mouseCol = col;
		mouseRow = row;
	}
	
	/**
	 * Draw the board
	 * onto the given Graphics object
	 * @param g
	 */
	public void makeImage(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int row = 5;
		int col = 0;
		int lastCol = model.getLastMoveCol();
		int lastRow = -1;
		int count = model.getCount();
		if (lastCol != -1) {
			lastRow = 6 - model.getFirstEmpty(lastCol);
		}
		
		
		
		setBackground(Color.LIGHT_GRAY);
		

		Grid grid = model.getGrid();
		while (col < 7) {
			row = 5;
			while (row >= 0) {
				Color c = Color.WHITE;
				switch (grid.getCell(col, 5-row)) {
				case 1:
					c = player1Color;
					break;
				case 2:
					c = player2Color;
					break;
				}
				if (col == lastCol && row == lastRow) {
					c = Color.WHITE;
				} else if (col == mouseCol && 5 - row == mouseRow) {
					c = c.darker().darker();
				}
				drawToken(g2d, c, row, col);
				row--;
			}
			col++;
		}
		/*
		while (counterCol < 7) {
			counterRow = 5;
			y = 0;
			while (counterRow >= 0) {
				if (grid.getCell(counterCol,counterRow) == 0 ) {//for getIndex method we can do things in a similar way
					g2d.setColor(Color.WHITE);// we can use this line to control color later
				} else if (grid.getCell(counterCol,counterRow) == 1) {
					g2d.setColor(player1Color);
				} else if (grid.getCell(counterCol,counterRow) == 2) {
					g2d.setColor(player2Color);
				}
				g2d.fillOval(x, y, r, r);
				y=y+100;
				counterRow--;
			}
			x=x+100;
			counterCol++;
		}
		*/
		/*g2d.setColor(player1Color);
		  g2d.fillOval(750, 200, r, r);
		  g2d.setColor(player2Color);
		  g2d.fillOval(750, 350, r, r);*/
		if (model.getCurrentPlayer() == 1) {
			g2d.setColor(player1Color);
			g2d.fillOval(0, 635, 80, 80);
		} else {
			g2d.setColor(player2Color);
			g2d.fillOval(0, 635, 80, 80);
		}
	}
	
    public void animation(Graphics g, GameModel model) {
    //todo: Martin
       Graphics2D g2d = (Graphics2D) g;
       Grid grid = model.getGrid();
       int col = model.getLastMoveCol();
       int row = 0;
       int count = 0;
       Color c = Color.WHITE;
       
       if (col != -1) {
	        row = model.getFirstEmpty(col) - 1;
	        count = model.getCount();
	        if(grid.getCell(col, row)==1){        //get color of circle
	        	c = player1Color;
	        	g2d.setColor(player1Color);
	        }else if(grid.getCell(col, row)==2){
	            c = player2Color;
	            g2d.setColor(player2Color);
	        }
	        g2d.fillOval(col*102, count, 90, 90);
	        if(count == (5-row)*100){
	        	g2d.setColor(Color.LIGHT_GRAY);
	        	g2d.fillOval(col*102, count, 90, 90);
	        	c = c.darker().darker().darker();
	        	drawToken(g2d, c, (5-row), col);
	        }
       }
    }

	/**
	 * Draw the info box.
	 */
	public void makeLabel() {
		infoBoard.setBounds(0, 510, 200, 200);
		infoBoard.setFont(new Font("info",2,18));
		infoBoard.setText("Info Board :");
		turn.setBounds(0, 530, 200, 200);
		if (model.getCurrentPlayer() == 1 ) {
			turn.setText("Player 1's turn");
		} else {
			turn.setText("Player 2's turn");
		}
		gameModel.setBounds(200,500,300,300);
		gameModel.setFont(new Font("info",3,18));
		if(players[0] instanceof HumanAgent&&players[1] instanceof HumanAgent){
			gameModel.setText("Human V.S Human");
		}else if(players[0] instanceof HumanAgent&&players[1] instanceof BeastAgent){
			if(players[1].equals(new BeastAgent(BeastAgent.EASY))){
				gameModel.setText("Human V.S Easy AI");
			}else if(players[1].equals(new BeastAgent(BeastAgent.MEDIUM))){
				gameModel.setText("Human V.S Intermediate AI");
			}else if(players[1].equals(new BeastAgent(BeastAgent.HARD))){
				gameModel.setText("Human V.S Expert AI");
			}
		}else if(players[0] instanceof BeastAgent&&players[1] instanceof HumanAgent){
			if(players[0].equals(new BeastAgent(BeastAgent.EASY))){
				gameModel.setText("Easy AI V.S Human");
			}else if(players[0].equals(new BeastAgent(BeastAgent.MEDIUM))){
				gameModel.setText("Intermediate AI V.S Human");
			}else if(players[0].equals(new BeastAgent(BeastAgent.HARD))){
				gameModel.setText("Expert AI V.S Human");
			}
		}else{
			gameModel.setText("Online Game");
		}
		add(infoBoard);
		add(turn);
		add(gameModel);
	}
	
	/**
	 * paints the entire display to the given Graphics object
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		makeImage(g);
		makeLabel();
		animation(g,model);
	}



}
