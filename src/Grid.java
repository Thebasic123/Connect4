/**
 * A basic representation of a 2d grid of cells
 * A thin abstraction over an int[][].
 * @author darwinvickers
 *
 */
public class Grid {
	private int[][] cells;
	
	private final int rows;
	private final int columns;

	/**
	 * Make a new Grid of specified size.
	 * Size can't be changed.
	 * @param rows
	 * @param columns
	 */
	public Grid(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		cells = new int[this.columns][this.rows];
	}
	
	/**
	 * Copy constructor
	 * @param grid
	 */
	public Grid(Grid grid) {
		rows = grid.rows;
		columns = grid.columns;
		cells = new int[columns][rows];
		for(int col = 0; col < columns; col++) {
		    cells[col] = grid.cells[col].clone();
		}
	}
	
	/**
	 * Set the value of a given cell
	 * @param col
	 * @param row
	 * @param value
	 */
	public void setCell(int col, int row, int value) {
		cells[col][row] = value;
	}
	
	/**
	 * Get the value of a given cell.
	 * @param col
	 * @param row
	 * @return
	 */
	public int getCell(int col, int row) {
		return cells[col][row];
	}
	
	/**
	 * Check if the specified position lies within the
	 * dimensions of the grid.
	 * @param col
	 * @param row
	 * @return
	 */
	public boolean contains(int col, int row) {
		return 0 <= col && col < columns && 0 <= row && row < rows;
	}
}
