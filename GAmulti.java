import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/*
 * fitness function -> sum of heuristics
 * 
 * population : different variants of the weights
 * 
 */

public class GAmulti implements Runnable {

	// population of 20 with 4 weights each. 
	static float[][] population;
	double[] fitnessScores;
	int index;
	long startTime;
	long endTime;

	static int popNum;
	static int weightsNum;
	float[] weights;
	static Random rand = new Random();

	//double[] w = {-1, -1.5, 2, -0.5};

	static boolean isLastMove = false;

	public GAmulti(float[][] population2, double[] fitnessScores2, int i) {
		// TODO Auto-generated constructor stub
		population = population2;
		fitnessScores = fitnessScores2;
		popNum = population2.length;
		index = i;
		weights = population2[index];
		weightsNum = weights.length;
		startTime = System.nanoTime();
	}

	public double fitnessFunc(TempState s, float[] w) {
		if (s.getHasLost()) {
			return Integer.MIN_VALUE;
		} else {

			// 1 - landing height
			// 2 - rows cleared in total
			// 3 - row transitions
			// 4 - column transitions
			// 5 - #holes
			// 6 - well sums
			
			int h1 = s.getLandingHeight();
			int h2 = s.getRowsCleared();
			int h3 = s.getRowTransitions();
			int h4 = s.getColTransitions();
			int h5 = s.getNoOfHoles();
			int h6 = s.getWellSum();
			
			double h = w[0] * h1 + w[1] * h2
					+ w[2] * h3 + w[3] * h4 
					+ w[4] * h5 + w[5] * h6; 
			return h;

		}
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		int validMovesNum = legalMoves.length;
		int toMove = 0;
		double scoreToMove = Integer.MIN_VALUE;
		
		for (int i=0; i<validMovesNum; i++) {
			TempState stateCopy = new TempState(s);
			stateCopy.makeMove(legalMoves[i]);

			// pick the move with the largest score.
			double nextScore = fitnessFunc(stateCopy, weights);
			if (nextScore > scoreToMove) {
				scoreToMove = nextScore;
				toMove = i;
			}
		}
		return toMove;
	}

	private void setFitnessScore(int index, int linesCleared) {
		fitnessScores[index] = linesCleared;
	}

	public void run() {

		// for each weight, play the game.
		State s = new State();
//		new TFrame(s);

		while(!s.hasLost()) {
			s.makeMove(pickMove(s,s.legalMoves()));
//			s.draw();
//			s.drawNext(0,0);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		setFitnessScore(index, s.getRowsCleared());
		System.out.println("i=" + index + "; weights: " + Arrays.toString(population[index]));
		System.out.println("i=" + index + "; # rows : " + s.getRowsCleared());
		//				System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		
		endTime = System.nanoTime();
		

				
				
		System.out.println("time taken: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
		System.out.println("done index " + index);
		
	}
}
