
public class TempState extends State {

	private int turnsub = 0;
	private int clearedsub = 0;
	private int clearedThisTurn = 0;

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
	
	public int getRowsCleared() {
		return clearedsub;
	}

	public int getRowsClearedThisTurn() {
		return clearedThisTurn;
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




}
