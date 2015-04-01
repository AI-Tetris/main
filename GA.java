import java.util.Arrays;
import java.util.Random;


/*
 * fitness function -> sum of heuristics
 * 
 * population : different variants of the weights
 * 
 * heuristics:
 * 
 * 1- average column height
 * 2- highest height
 * 3- #cleared lines
 * 4- #holes
 *
 * 
 */




public class GA {

	// population of 20 with 4 weights each. 
	static int popNum;
	static int weightsNum = 5;
	static float[][] population;
	static Random rand = new Random();

	//double[] w = {-1, -1.5, 2, -0.5};

	// scores[i] : highest score for next move, given weights[i]
	double[] scores;

	// moves[i]  : moves[i] gives the move # for weights[i] to get scores[i]
	int[] moves;

	int[] keep;
	int keepcounter = 0;
	boolean isLastMove = false;

	public GA(int n) {
		popNum = n;
		population = new float[popNum][weightsNum];
		scores = new double[popNum];
		moves = new int[popNum];
		keep = new int[popNum];
		initPop();
	}

	public GA(float[][] specifiedPop) {
		popNum = specifiedPop.length;
		population = specifiedPop;
		scores = new double[popNum];
		moves = new int[popNum];
		keep = new int[popNum];
	}

	public float[][] getPop() {
		float[][] pop = new float[popNum][weightsNum];
		for (int i=0; i<popNum; i++) {
			pop[i] = (float[]) population[i].clone();
		}
		return pop;
	}

	public static void initPop() {

		for (int i=0; i<popNum;i++) {

			// weights are randomly calculated with a range of 1 (eg 0 < x < 1)
			// 1- avg col height (-)
			// 2- highest height (-)
			// 3- #cleared lines per turn (+)
			// 4- #holes (-)
			// 5- #blockages (-)

			float[] newWeights = {(float) (rand.nextFloat())-1, (float) (rand.nextFloat())-1,
					(float) (rand.nextFloat()), (float) (rand.nextFloat())-1,
					(float) (rand.nextFloat()-1)};

			population[i] = newWeights;
		}
	}

	public double fitnessFunc(TempState s, float[] w) {


		if (s.getHasLost()) {
			return Integer.MIN_VALUE;
		} else {
			int[][] stateField = s.getField();
			// 1- avg col height
			// 2- highest height
			// 3- #cleared lines per turn
			// 4- #holes
			// 5- #blockages

			float avgH = 0;
			int highest = 0;
			int linesCleared = s.getRowsClearedThisTurn();
			int holes = 0;
			int blockages = 0;
			
			
			
			for (int c=9; c<=0; c--) {
				boolean blocked = false; // # blocks, can contain any number of holes
				boolean holed = false; // each hole where the top is filled;

				for (int r=19; r<=0; r--) {
					if (!blocked) {
						if (stateField[r][c] != 0) {
							blocked = !blocked;
						}
					} else {
						if (stateField[r][c] == 0) {
							blockages++;
							blocked = !blocked;
						} 
					}

					if (!holed) {
						if (stateField[r][c] != 0) {
							holed = !holed;
						}
					} else {
						if (stateField[r][c] == 0) {
							holes++;
						}
					}
				}
			}



			int[] columns = s.getTop();
			for (int i=0; i<10; i++) {
				avgH += columns[i];

				if (columns[i] > highest) {
					highest = columns[i]; 
				}
			}
			avgH = (float) (avgH/10.0);
			
			double totalValue = w[0]*avgH + w[1]*highest
					+ w[2]*linesCleared + w[3]*holes + w[4]*blockages;

			return totalValue;
		}
	}
	
	private String invert(String s) {
		if (s.equals("1")) {
			return "0";
		} else {
			return "1";
		}
	}
	
	private float[] recombination(float[] parent1w, float[] parent2w) {
		// a simple crossover.

		float[] newWeight = new float[weightsNum];

		for (int i=0; i<weightsNum; i++) {

			
			
			
			
			
			/****
			****/
			
			

			boolean done = false;
			while (!done) {

				float w1 = parent1w[i];
				int intBits1 = Float.floatToIntBits(w1);

				float w2 = parent2w[i];
				int intBits2 = Float.floatToIntBits(w2);

				
				/****
				****/

				String s1 = Integer.toBinaryString(intBits1);
				String ts1 = "";
				for (int m=0; m<32-s1.length(); m++) {
					ts1 += "0";
				}
				s1 = ts1 + s1;
				
				
				String s2 = Integer.toBinaryString(intBits2);
				String ts2 = "";
				for (int m=0; m<32-s2.length(); m++) {
					ts2 += "0";
				}
				s2 = ts2 + s2;

				String s3 = "";
				for (int b=0; b<32; b++) {
					if (s1.substring(b, b+1).equals(s2.substring(b, b+1))) {
						s3 = s3 + s1.substring(b, b+1);
					} else {
						float randf = rand.nextFloat();
						if (randf < 0.5) {
							s3 = s3 + "0";
						} else {
							s3 = s3 + "1";
						}
					}
				}
				
				
				// 1% mutation --> invert random bits
				float randf = rand.nextFloat(); 
				while (randf < 0.02) {
					int randint = rand.nextInt(32);
					
					s3 = s3.substring(0, randint) 
							+ invert(s3.substring(randint, randint+1))
							+ s3.substring(randint+1, 32);
					
					randf = rand.nextFloat();
				}
				
				
//				float ww3 = Float.intBitsToFloat((int) Long.parseLong(s3, 2));
				int intBits3 = (int) Long.parseLong(s3, 2);
				
//				System.out.println(intBits1);
//				System.out.println(intBits2);
//				System.out.println(intBits3 + "\n");
//				System.out.println(s1);
//				System.out.println(s2);
//				System.out.println(s3 + "\n");
				
				
				float w3 = Float.intBitsToFloat(intBits3);
				//mutation
				/****
				****/


				newWeight[i] = w3;

				

				
				
				if (Float.isNaN(w3)) {
					done = false;
				} else {
					done = true;
				}
			}
		}

		return newWeight;
	}

	private void selection() {
		// choose <half the population to throw away, and breed from the remaining.

		boolean selected = false;

		while (!selected) {
			int randint = rand.nextInt(popNum);
			keepcounter = 0;
			double val = scores[randint];

			for (int i=0; i<popNum; i++) {
				if (scores[i] >= val) {
					keep[keepcounter] = i;
					keepcounter++;
				}
			}
			
			if (keepcounter == popNum) {
				// all get the same value. i.e. lost. 
				selected = true;
				isLastMove = true;
			}
			if ((keepcounter >= 0.4*popNum) && (keepcounter <= 0.6*popNum)) {
				selected = true;
			}
		}

	}

	private void makeNewPop() {
		float[][] newPop = new float[popNum][weightsNum];
		for (int j=0; j<popNum; j++) {
			int randint1 = rand.nextInt(keepcounter); 
			int randint2 = rand.nextInt(keepcounter);

			newPop[j] = recombination(population[randint1], population[randint2]);
		}		
		population = newPop;		
	}

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		int validMovesNum = legalMoves.length;
		double maxFitnessScore = Integer.MIN_VALUE;
		int maxFitnessIndex = 0;

		for (int k=0; k<popNum; k++) {
			double fitnessScoreCurr = Integer.MIN_VALUE;
			moves[k] = 0;
			
			for (int i=0; i<validMovesNum; i++) {
				TempState stateCopy = new TempState(s);
				stateCopy.makeMove(legalMoves[i]);

				// call the fitness function for each legal move 
				// and pick the move with the largest score.

				double nextScore = fitnessFunc(stateCopy, population[k]);
				if (nextScore > fitnessScoreCurr) {
					fitnessScoreCurr = nextScore;
					moves[k] = i;
				}						
			}

			scores[k] = fitnessScoreCurr;
			if (fitnessScoreCurr > maxFitnessScore) {
				maxFitnessScore = fitnessScoreCurr;
				maxFitnessIndex = k;
			}
		}			

		// pick to throw away <half of the population
		selection();

		// choose next move to make (max fitness function).
		System.out.println("population: " + Arrays.deepToString(population) + "\n");

		// breed next population
		makeNewPop();

		return moves[maxFitnessIndex];

	}

	public static void main(String[] args) {
//		State s = new State();
//		new TFrame(s);

		int numIterations = 10;
		int numPop = 40;
		float[][] ownPop = {{-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834229f, -0.6224023f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.013977f, -2.1521964E-23f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141335f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834229f, -0.6224023f, -0.25352478f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141029f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}};
//		float[][] popu = {{-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834229f, -0.6224023f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.013977f, -2.1521964E-23f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141335f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0141296f, -4.0463955E-4f, -1.4192376f}, {-0.6834229f, -0.6224023f, -0.25352478f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -1.0140991f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6224023f, -1.0141029f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50705147f, -4.0463955E-4f, -1.4192376f}, {-0.6834191f, -0.6223985f, -0.50704956f, -4.0463955E-4f, -1.4192376f}};
		float[][] popu = new float[numPop][5];

		GA p;
		for (int i=0; i<numIterations; i++) {
			State s = new State();
			new TFrame(s);
			if (i==0) {
				p = new GA(20);

			} else {
				p = new GA(popu);

			}

			//		}
		////		GA p = new GA(20);
			//		GA p = new GA(ownPop);

			while(!s.hasLost()) {
				s.makeMove(p.pickMove(s,s.legalMoves()));
				s.draw();
				s.drawNext(0,0);
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			popu = p.getPop();
			System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		}
	}




}

