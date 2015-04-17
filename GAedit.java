import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


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
	double[] fitnessScores;
	int totalFitnessScore = 0;
	
	static int popNum;
	static int weightsNum = 6;
	static float[] weights;
	static Random rand = new Random();

	//double[] w = {-1, -1.5, 2, -0.5};


	public GAedit(int n) {
		popNum = n;
		population = new float[popNum][weightsNum];
		fitnessScores = new double[popNum];
		initPop();
	}

	public GAedit(float[][] specifiedPop) {
		popNum = specifiedPop.length;
		population = specifiedPop;
		fitnessScores = new double[popNum];
	}

	public void initPop() {

		for (int i=0; i<popNum;i++) {

			// weights are randomly calculated with a range of 1 (eg 0 < x < 1)			
			
			// 1 - landing height (-)
			// 2 - rows cleared in total (+)
			// 3 - row transitions (?) 
			// 4 - column transitions (?)
			// 5 - #holes (-)
			// 6 - well sums (?)

			int r = 10; // give a larger range for the algo to update weights
//			float[] newWeights = 
//		       {(float) (rand.nextFloat()-1)*r, 
//				(float) (rand.nextFloat())*r, 
//				(float) (rand.nextFloat()-0.5)*r,
//				(float) (rand.nextFloat()-0.5)*r,
//				(float) (rand.nextFloat()-1)*r,
//				(float) (rand.nextFloat()-0.5)*r};
		
		float[] newWeights = 
		       {(float) (rand.nextFloat()-0.5)*r, 
				(float) (rand.nextFloat()-0.5)*r, 
				(float) (rand.nextFloat()-0.5)*r,
				(float) (rand.nextFloat()-0.5)*r,
				(float) (rand.nextFloat()-0.5)*r,
				(float) (rand.nextFloat()-0.5)*r};
			
			population[i] = newWeights;
			
		}
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
	
	private String invert(String s) {
		if (s.equals("1")) {
			return "0";
		} else {
			return "1";
		}
	}
	
	private float[] recombination(float[] parent1w, float[] parent2w, float[] parent3w) {
		// a simple combination of parents using bits.

		float[] newWeight = new float[weightsNum];

		for (int i=0; i<weightsNum; i++) {

			boolean done = false;
			while (!done) {

				float w1 = parent1w[i];
				int intBits1 = Float.floatToIntBits(w1);

				float w2 = parent2w[i];
				int intBits2 = Float.floatToIntBits(w2);
				
				float w3 = parent3w[i];
				int intBits3 = Float.floatToIntBits(w3);

				// add leading 0s to make 32-bit
				String s1 = Integer.toBinaryString(intBits1);
				String ts1 = ""; 
				for (int m=0; m<32-s1.length(); m++) {
					ts1 += "0";
				}
				s1 = ts1 + s1;
				
				// add leading 0s to make 32-bit
				String s2 = Integer.toBinaryString(intBits2);
				String ts2 = ""; 
				for (int m=0; m<32-s2.length(); m++) {
					ts2 += "0";
				}
				s2 = ts2 + s2;

				// add leading 0s to make 32-bit
				String s3 = Integer.toBinaryString(intBits3);
				String ts3 = ""; 
				for (int m=0; m<32-s3.length(); m++) {
					ts3 += "0";
				}
				s3 = ts3 + s3;

				// combination of bits
				String sNew = "";
				for (int b=0; b<32; b++) {
					if (s1.substring(b, b+1).equals(s2.substring(b, b+1))) {
						sNew = sNew + s1.substring(b, b+1);
					} else if (s1.substring(b, b+1).equals(s3.substring(b, b+1))) {
						sNew = sNew + s1.substring(b, b+1);
					} else {
						sNew = sNew + s2.substring(b, b+1);
					}
				}
				
				// 10% mutation --> invert random bits
				float randf = rand.nextFloat(); 
				while (randf < 0.1) {
					int randint = rand.nextInt(32);
					
					sNew = sNew.substring(0, randint) 
							+ invert(sNew.substring(randint, randint+1))
							+ sNew.substring(randint+1, 32);
					
					randf = rand.nextFloat();
				}
				
				int intBitsNew = (int) Long.parseLong(sNew, 2);
				float wNew = Float.intBitsToFloat(intBitsNew);
				newWeight[i] = wNew;
				
				if (Float.isNaN(wNew)) {
					done = false;
				} else {
					done = true;
				}
			}
		}

		return newWeight;
	}

	private int findParentIndex(float normValue, int startIndex, int endIndex) {
		// returns the first parent index with a normalised fitness value > normvalue
		// could be further optimised to use quick-select.
		
		for (int i=0; i<popNum; i++) {
			double val = fitnessScores[i];
			
			if (normValue <= val) {
				return i;
			}
		}

		System.out.println("Something is wrong");
		return 0;		
	}
	
	private void selection() {
		// calculating the normalised fitness score to select population by percentage. 
		
		double normalisedScore = (float) fitnessScores[0] / totalFitnessScore;
		double accNorScore = normalisedScore;
		fitnessScores[0] = accNorScore;
		
		for (int i=1; i<popNum; i++) {
			normalisedScore = (double) fitnessScores[i] / totalFitnessScore;
			accNorScore = fitnessScores[i-1] + normalisedScore;
			fitnessScores[i] = accNorScore;
		}
	}

	private void makeNewPop() {
		
		float[][] newPop = new float[popNum][weightsNum];
		for (int j=0; j<popNum; j++) {
			int parent1Index = findParentIndex(rand.nextFloat(), 0, popNum-1); 
			int parent2Index = findParentIndex(rand.nextFloat(), 0, popNum-1);
			int parent3Index = findParentIndex(rand.nextFloat(), 0, popNum-1);

			newPop[j] = recombination(population[parent1Index], population[parent2Index], population[parent3Index]);
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
		totalFitnessScore += linesCleared;
	}
	
	private void resetTotalFitnessScore() {
		totalFitnessScore = 0;
	}

	public static void main(String[] args) {
		
		
		int numPop = 30; // #games each iteration
		int numIterations = 50;

		float[][] ownPop = {{-0.9165944f, 0.56823504f, -0.2590816f, -0.65175056f, -0.99231493f, -0.86575115f, -0.5795858f}, {-2.1023023f, 3.9569716f, 7.411435f, -1.6528571f, -4.468202f, -0.31573534f, -2.545408f}, {-1.2519568f, 6.5325665f, 3.8953888f, -2.7684522f, -3.7966685f, -2.6728182f, -0.0107795f}, {-3.526153f, 1.9817466f, 9.340173f, -2.7328682f, -4.529084f, -4.9612017f, -3.588602f}, {-3.0559034f, 3.9422965f, 4.5888333f, -2.4691563f, -3.7622814f, -9.761342f, 0.5290121f}, {-2.8965187f, 2.1095533f, 0.22385538f, -2.8503423f, -3.320928f, -8.042766f, -2.1487074f}, {-7.381353f, 3.279231f, 2.290802f, -3.130217f, -2.7132468f, -7.457886f, -1.1826926f}, {-0.21109468f, 0.38342965f, -0.018973954f, 0.014483638f, -0.459157f, -1.661284f, -0.4257102f}};

		GAedit p = new GAedit(numPop);
//		GAedit p = new GAedit(ownPop);
		
		
		long totalStartTime;
		long totalEndTime;
		
		long startTime;
		long endTime;
		
		totalStartTime = System.nanoTime();

		// iterate times to update the population
		for (int j=0; j<numIterations; j++) {
			
			System.out.println("population: " + Arrays.deepToString(population));
			
			System.out.println("game #" + (j+1));
			p.resetTotalFitnessScore();
			
			// for each weight, play the game.
			for (int i=0; i<GAedit.popNum; i++) {
				
				startTime = System.nanoTime();

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
				System.out.println("i=" + i + "; weights: " + Arrays.toString(population[i]));
				System.out.println("i=" + i + "; # rows : " + s.getRowsCleared());
//				System.out.println("You have completed "+s.getRowsCleared()+" rows.");
				
				endTime = System.nanoTime();
				
				System.out.println("time taken: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
			}
			
			// games all over, now update the population for the next iteration.
			p.selection();
			p.makeNewPop();
			
		}
		
		totalEndTime = System.nanoTime();
		
		System.out.println("total time taken: " + TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime));

		System.out.println("done");
	}
}