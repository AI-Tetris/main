public class TempPlayer {

	int[][] moves;
	int[] move; // move[0] = orientation, move[1] = slot
	int[][] field;
	int[][] currField;
	int nextPiece;
	int turn;
	int[] top;
	int[] currTop;
	double[] coeff;
	TempState tempState;
	public static final int ORIENT = 0;
	public static final int SLOT = 1;

	public TempPlayer(State s, int[][] legalMoves, double[] newCoeff) {
		moves = legalMoves;
		field = s.getField();
		nextPiece = s.getNextPiece();
		turn = s.getTurnNumber();
		top = s.getTop();
		coeff = newCoeff;
	}

	// initialise (or re-initialise) tempState
	public void initTemp() {
		// create deep copy of field - only values, not references
		currField = new int[State.ROWS][State.COLS];
		for (int j = 0; j < State.ROWS; j++) {
			for (int k = 0; k < State.COLS; k++) {
				currField[j][k] = field[j][k];
			}
		}

		currTop = new int[top.length];
		for (int i = 0; i < top.length; i++) {
			currTop[i] = top[i];
		}

		tempState = new TempState(nextPiece, currField, turn, currTop);
	}

	// get move
	public int getMove() {
		// legalMoves[column no] [orientation] specifies move

		double bestH = -Double.MAX_VALUE;
		int bestMove = 0;

		for (int i = 0; i < moves.length; i++) {
			// initialise new state with next piece and current field
			initTemp();
			move = moves[i];

			// make move and update state
			tempState.makeMove(move);
			currField = tempState.getField();

			// heuristic function factors: landing height, rows cleared, lateral
			// &
			// vertical bumpiness, number of holes and wells
			// For simplicity, these are labelled h1-6
			int h1 = getLandingHeight();
			int h2 = getRowsCleared();
			int h3 = getRowTransitions();
			int h4 = getColTransitions();
			int h5 = getNoOfHoles();
			int h6 = getWellSum();
			
			double h = coeff[0] * h1 + coeff[1] * h2
					+ coeff[2] * h3 + coeff[3] * h4
					+ coeff[4] * h5 + coeff[5] * h6;

			
			// double h = -h1;
			if (h > bestH) {
				bestH = h;
				bestMove = i;
			}

		}
		return bestMove;
	}

	// get heuristics

	// height of column when landed
	public int getLandingHeight() {
		// get column height
		int col = move[SLOT];

		int width = State.pWidth[nextPiece][move[ORIENT]];

		int colHeight;
		int maxColHeight = 0;

		// get maximum column height landed on
		for (int i = 0; i < width; i++) {
			colHeight = State.ROWS - 1;
			while (col + i < State.COLS && currField[colHeight][col + i] == 0
					&& colHeight > 0) {
				colHeight--;
			}

			if (colHeight > maxColHeight) {
				maxColHeight = colHeight;
			}

		}
		return maxColHeight + 1;
	}

	public int getRowsCleared() {
		return tempState.getRowsCleared();
	}

	// lateral bumpiness
	public int getRowTransitions() {
		int transitions = 0;
		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 0; j < State.COLS - 1; j++) {
				// adjacent cells are not same in filled status
				if (currField[i][j] == 0 && currField[i][j + 1] != 0
						|| currField[i][j] != 0 && currField[i][j + 1] == 0) {
					transitions++;
				}
			}
		}
		return transitions;
	}

	// vertical bumpiness
	public int getColTransitions() {
		int transitions = 0;
		for (int i = 0; i < State.ROWS - 1; i++) {
			for (int j = 0; j < State.COLS; j++) {
				// adjacent cells are not same in filled status
				if (currField[i][j] == 0 && currField[i + 1][j] != 0
						|| currField[i][j] != 0 && currField[i + 1][j] == 0) {
					transitions++;
				}
			}
		}
		return transitions;
	}

	// no of holes
	public int getNoOfHoles() {
		int holes = 0;
		// don't need to check top row for holes
		for (int i = 0; i < State.ROWS - 1; i++) {
			for (int j = 0; j < State.COLS; j++) {
				// empty spaces with filled cell above
				if (currField[i][j] == 0 && currField[i + 1][j] != 0) {
					holes++;
				}
			}
		}
		return holes;
	}

	// well sums => empty cells with filled adjacent columns
	public int getWellSum() {
		int wells = 0;
		// don't need to check top row for holes
		for (int i = 0; i < State.ROWS; i++) {
			for (int j = 1; j < State.COLS - 1; j++) {
				// empty spaces with filled left & right cols
				if ((currField[i][j] == 0) && (currField[i][j - 1] != 0)
						&& (currField[i][j + 1] != 0)) {
					wells++;
				}
			}
		}

		// corner cases
		for (int i = 0; i < State.ROWS; i++) {
			// empty spaces with filled left & right cols
			if ((currField[i][0] == 0) && (currField[i][1] != 0)) {
				wells++;
			}
		}

		for (int i = 0; i < State.ROWS; i++) {
			// empty spaces with filled left & right cols
			if ((currField[i][State.COLS - 1] == 0)
					&& (currField[i][State.COLS - 2] != 0)) {
				wells++;
			}
		}
		return wells;
	}

	// print field for debugging
	public void printField() {
		for (int i = currField.length - 1; i >= 0; i--) {
			for (int j = 0; j < currField[0].length; j++) {
				System.out.print(currField[i][j]);
			}
			System.out.println();
		}
	}

	public void main(String[] args) {
		// new TFrame(tempState);
	}

}
