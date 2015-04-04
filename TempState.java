
public class TempState extends State {

	private int turnsub = 0;
	private int clearedsub = 0;
	private int clearedThisTurn = 0;
	private int[] lastMove;

	//each square in the grid - int means empty - other values mean the turn it was placed
	private int[][] fieldsub = new int[ROWS][COLS];
	//top row+1 of each column
	//0 means empty
	private int[] topsub = new int[COLS];
	
	private boolean lostsub = false;


	//possible orientations for a given piece type
	protected static int[] pOrients = {1,2,4,4,4,2,2};

	//the next several arrays define the piece vocabulary in detail
	//width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = {
		{2},
		{1,4},
		{2,3,2,3},
		{2,3,2,3},
		{2,3,2,3},
		{3,2},
		{3,2}
	};
	//height of the pieces [piece ID][orientation]
	private static int[][] pHeight = {
		{2},
		{4,1},
		{3,2,3,2},
		{3,2,3,2},
		{3,2,3,2},
		{2,3},
		{2,3}
	};
	private static int[][][] pBottom = {
		{{0,0}},
		{{0},{0,0,0,0}},
		{{0,0},{0,1,1},{2,0},{0,0,0}},
		{{0,0},{0,0,0},{0,2},{1,1,0}},
		{{0,1},{1,0,1},{1,0},{0,0,0}},
		{{0,0,1},{1,0}},
		{{1,0,0},{0,1}}
	};
	private static int[][][] pTop = {
		{{2,2}},
		{{4},{1,1,1,1}},
		{{3,1},{2,2,2},{3,3},{1,1,2}},
		{{1,3},{2,1,1},{3,3},{2,2,2}},
		{{3,2},{2,2,2},{2,3},{1,2,1}},
		{{1,2,2},{3,2}},
		{{2,2,1},{2,3}}
	};


	public TempState(State s) {
		// TODO Auto-generated constructor stub

		int[][] originalField = s.getField();
		for (int i=0; i<originalField.length; i++) {
			fieldsub[i] = (int[]) originalField[i].clone();
		}

		topsub = (int[]) s.getTop().clone();

		pOrients = State.getpOrients();
		pWidth = State.getpWidth();
		pHeight= State.getpHeight();
		pBottom = State.getpBottom();
		pTop = State.getpTop();

		nextPiece = s.getNextPiece();

		clearedsub = s.getRowsCleared();
		turnsub = s.getTurnNumber();


	}
	
	public int getTurnNumber() {
		return turnsub;
	}
	
	public int getNextPiece() {
		return nextPiece;
	}
	public int[][] getField() {
		return fieldsub;
	}
	
	public int[] getTop() {
		return topsub;
	}

	public boolean getHasLost() {
		return lostsub;
	}
	
	//make a move based on the move index - its order in the legalMoves list
	public void makeMove(int move) {
		makeMove(legalMoves[nextPiece][move]);
	}

	//make a move based on an array of orient and slot
	public void makeMove(int[] move) {
		lastMove = move;
		makeMove(move[ORIENT],move[SLOT]);
	}

	//returns false if you lose - true otherwise
	public boolean makeMove(int orient, int slot) {
		turnsub++;
		//height if the first column makes contact
		int height = topsub[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,topsub[slot+c]-pBottom[nextPiece][orient][c]);
		}

		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lostsub = true;
			return false;
		}


		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {

			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				fieldsub[h][i+slot] = turnsub;
			}
		}

		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			topsub[slot+c]=height+pTop[nextPiece][orient][c];
		}

		clearedThisTurn = 0;

		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(fieldsub[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				clearedThisTurn++;
				clearedsub++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < topsub[c]; i++) {
						fieldsub[i][c] = fieldsub[i+1][c];
					}
					//lower the top
					topsub[c]--;
					while(topsub[c]>=1 && fieldsub[topsub[c]-1][c]==0)	topsub[c]--;
				}
			}
		}			


		return true;
	}

	
	// heuristic h1: height of column when landed
	public int getLandingHeight() {
		// get column height
		int col = lastMove[SLOT];

		int width = State.pWidth[nextPiece][lastMove[ORIENT]];

		int colHeight;
		int maxColHeight = 0;

		// get maximum column height landed on
		for (int i = 0; i < width; i++) {
			colHeight = State.ROWS - 1;
			while (col + i < State.COLS && fieldsub[colHeight][col + i] == 0
					&& colHeight > 0) {
				colHeight--;
			}

			if (colHeight > maxColHeight) {
				maxColHeight = colHeight;
			}

		}
		return maxColHeight + 1;
	}
	
	// heuristic h2a: total rows cleared
	public int getRowsCleared() {
		return clearedsub;
	}

	// heuristic h2b: rows cleared by the last move
	public int getRowsClearedThisTurn() {
		return clearedThisTurn;
	}
	
	// heuristic h3: lateral bumpiness
	public int getRowTransitions() {
		int transitions = 0;
		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 0; j < State.COLS - 1; j++) {
				// adjacent cells are not same in filled status
				if (fieldsub[i][j] == 0 && fieldsub[i][j + 1] != 0
						|| fieldsub[i][j] != 0 && fieldsub[i][j + 1] == 0) {
					transitions++;
				}
			}
		}
		return transitions;
	}

	// heuristic h4: vertical bumpiness
	public int getColTransitions() {
		int transitions = 0;
		for (int i = 0; i < State.ROWS - 1; i++) {
			for (int j = 0; j < State.COLS; j++) {
				// adjacent cells are not same in filled status
				if (fieldsub[i][j] == 0 && fieldsub[i + 1][j] != 0
						|| fieldsub[i][j] != 0 && fieldsub[i + 1][j] == 0) {
					transitions++;
				}
			}
		}
		return transitions;
	}
	
	
	// heuristic h5: no of holes
	public int getNoOfHoles() {
		int holes = 0;
		// don't need to check top row for holes
		for (int i = 0; i < State.ROWS - 1; i++) {
			for (int j = 0; j < State.COLS; j++) {
				// empty spaces with filled cell above
				if (fieldsub[i][j] == 0 && fieldsub[i + 1][j] != 0) {
					holes++;
				}
			}
		}
		return holes;
	}

	
	// heuristic h6: well sums => empty cells with filled adjacent columns
	public int getWellSum() {
		int wells = 0;
		// don't need to check top row for holes
		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 1; j < State.COLS - 1; j++) {
				// empty spaces with filled left & right cols
				if ((fieldsub[i][j] == 0) && (fieldsub[i][j - 1] != 0)
						&& (fieldsub[i][j + 1] != 0)) {
					wells++;
				}
			}
		}

		// corner cases
		for (int i = 0; i < State.ROWS; i++) {
			// empty spaces with filled left & right cols
			if ((fieldsub[i][0] == 0) && (fieldsub[i][1] != 0)) {
				wells++;
			}
		}

		for (int i = 0; i < State.ROWS; i++) {
			// empty spaces with filled left & right cols
			if ((fieldsub[i][State.COLS - 1] == 0)
					&& (fieldsub[i][State.COLS - 2] != 0)) {
				wells++;
			}
		}
		return wells;
	}

}
