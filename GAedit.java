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




public class GAedit {

	// population of 20 with 4 weights each. 
	static float[][] population;
	int[] fitnessScores;
	static int popNum;
	static int weightsNum = 5;
	static float[] weights;
	static Random rand = new Random();

	//double[] w = {-1, -1.5, 2, -0.5};

	// scores[i] : highest score for next move, given weights[i]
	static double[] scores;

	// moves[i]  : moves[i] gives the move # for weights[i] to get scores[i]
	int[] moves;

	static int[] keep;
	static int keepcounter = 0;
	static boolean isLastMove = false;

	public GAedit(int n) {
		popNum = n;
		population = new float[popNum][weightsNum];
		fitnessScores = new int[popNum];
		scores = new double[popNum];
		moves = new int[popNum];
		keep = new int[popNum];
		initPop();
	}

	public GAedit(float[][] specifiedPop) {
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

	public void initPop() {

		for (int i=0; i<popNum;i++) {

			// weights are randomly calculated with a range of 1 (eg 0 < x < 1)
			// 1- avg col height (-)
			// 2- highest height (-)
			// 3- #cleared lines per turn (+)
			// 4- #holes (-)
			// 5- #blockages (-)

			int r = 20; //give a larger range for the algo to update weights
			float[] newWeights = {(float) (rand.nextFloat()-1)*r, (float) (rand.nextFloat()-1)*r,
					(float) (rand.nextFloat()*r), (float) (rand.nextFloat()-1)*r,
					(float) (rand.nextFloat()-1)*r};

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

			boolean done = false;
			while (!done) {

				float w1 = parent1w[i];
				int intBits1 = Float.floatToIntBits(w1);

				float w2 = parent2w[i];
				int intBits2 = Float.floatToIntBits(w2);

				String s1 = Integer.toBinaryString(intBits1);
				String ts1 = ""; // add leading 0s to make 32-bit
				for (int m=0; m<32-s1.length(); m++) {
					ts1 += "0";
				}
				s1 = ts1 + s1;
				
				
				String s2 = Integer.toBinaryString(intBits2);
				String ts2 = ""; // add leading 0s to make 32-bit
				for (int m=0; m<32-s2.length(); m++) {
					ts2 += "0";
				}
				s2 = ts2 + s2;

				// combination of bits
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
				
				// 2% mutation --> invert random bits
				float randf = rand.nextFloat(); 
				while (randf < 0.02) {
					int randint = rand.nextInt(32);
					
					s3 = s3.substring(0, randint) 
							+ invert(s3.substring(randint, randint+1))
							+ s3.substring(randint+1, 32);
					
					randf = rand.nextFloat();
				}
				
				int intBits3 = (int) Long.parseLong(s3, 2);
				float w3 = Float.intBitsToFloat(intBits3);
				newWeight[i] = w3;
				
				if (Float.isNaN(w3)) {
					done = false;
//				} else if (Math.abs(w3 - w1) > 20 && Math.abs(w3 - w2) > 20) {
					// too diff from both parents.
				} else {
					done = true;
				}
			}
		}

		return newWeight;
	}

	private void selection() {
		// select from ≈half the population to keep

		boolean selected = false;
		
		while (!selected) {
			int randint = rand.nextInt(popNum);
			keepcounter = 0;
			double val = fitnessScores[randint];
			
			for (int i=0; i<popNum; i++) {
				if (fitnessScores[i] >= val) {
					keep[keepcounter] = i;
					keepcounter++;
				}
			}
			
			if (keepcounter == popNum) {
				if (fitnessScores[(randint+1) % popNum] > val) {
					// current val is min, do again.
					
				} else {
					// all same value.
					selected = true;
				}
				
			} else if ((keepcounter >= 0.4*popNum) && (keepcounter <= 0.6*popNum)) {
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
	
	private void setWeight(float[] w) {
		weights = w;
	}
	
	private void setFitnessScore(int index, int linesCleared) {
		fitnessScores[index] = linesCleared;
	}

	public static void main(String[] args) {
//		State s = new State();
//		new TFrame(s);

		
		int numPop = 40; // #games each iteration
		int numIterations = 20;
//		float[][] ownPop = {{-0.18070294f, -0.7187392f, 5.5184228E-6f, 3.3093335E38f, -8.889216E-6f}, {-0.18070294f, -0.7109267f, 5.5146393E-6f, 3.307672E38f, -8.889216E-6f}, {-0.18070294f, -0.7109267f, 0.36165535f, 3.307672E38f, -8.889216E-6f}, {-0.18070294f, -0.7109267f, 5.514814E-6f, 3.307672E38f, -8.904131E-6f}, {-0.18070294f, -0.7109267f, 5.5184228E-6f, 3.307672E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 5.5146975E-6f, 3.3093335E38f, -8.889212E-6f}, {-0.1807039f, 0.7109267f, 5.514814E-6f, 3.307672E38f, -8.88923E-6f}, {-0.18070294f, -0.7109267f, 5.5146975E-6f, 3.307672E38f, -8.889212E-6f}, {-0.18070294f, -0.7109267f, 5.5184228E-6f, 3.307672E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 0.3614112f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 0.3614112f, 3.307672E38f, -8.889212E-6f}, {-0.18070294f, -0.7109267f, 5.5146975E-6f, 3.3093335E38f, -8.889216E-6f}, {-0.18070294f, -0.71873945f, 5.5146975E-6f, 3.3093335E38f, -8.889216E-6f}, {-0.18070294f, -0.7109267f, 5.5146975E-6f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 0.3614112f, 3.307672E38f, -8.889227E-6f}, {-0.18070294f, -0.7109267f, 5.5146975E-6f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 5.5146975E-6f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7187392f, 0.3614112f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7109267f, 0.3614112f, 3.3093335E38f, -8.889212E-6f}, {-0.18070294f, -0.7109267f, 5.5146975E-6f, 3.3093335E38f, -8.88923E-6f}};
//		float[][] popu = new float[numPop][5];

		GAedit p = new GAedit(numPop);

		// iterate times to update the population
		for (int j=0; j<numIterations; j++) {
			
			// for each weight, play the game.
			for (int i=0; i<GAedit.popNum; i++) {
				p.setWeight(GAedit.population[i]);
				State s = new State();
//				new TFrame(s);

				while(!s.hasLost()) {
					s.makeMove(p.pickMove(s,s.legalMoves()));
//					s.draw();
//					s.drawNext(0,0);
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				p.setFitnessScore(i, s.getRowsCleared());
//				System.out.println("You have completed "+s.getRowsCleared()+" rows.");
			}
			int s = 0;
			for (int z=0; z<numIterations; z++) {
				s += p.fitnessScores[z];
			}
			System.out.println(Arrays.deepToString(p.population));
			System.out.println("total: " + s);
			// games all over, now calculate.
			p.selection();
			p.makeNewPop();
			
		}
		System.out.println("done");
		////
	}




}

