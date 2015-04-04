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
	static int weightsNum = 7;
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
		fitnessScores = new int[popNum];
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
			
			
			// 1 - landing height 
			// 2a- rows cleared in total
			// 2b- rows cleared this round
			// 3 - row transitions
			// 4 - column transitions
			// 5 - #holes
			// 6 - well sums

			int r = 2; //give a larger range for the algo to update weights
//			float[] newWeights = {(float) (rand.nextFloat()-1)*r, (float) (rand.nextFloat()-1)*r,
//					(float) (rand.nextFloat()*r), (float) (rand.nextFloat()-1)*r,
//					(float) (rand.nextFloat()-1)*r};

			float[] newWeights = new float[weightsNum];
			for (int y=0; y<weightsNum; y++) {
				newWeights[y] = (float) (rand.nextFloat()-0.5) * r;
			}
			
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
			
			// 1 - landing height
			// 2a- rows cleared in total
			// 2b- rows cleared this round
			// 3 - row transitions
			// 4 - column transitions
			// 5 - #holes
			// 6 - well sums
			
			int h1 = s.getLandingHeight();
			int h2a = s.getRowsCleared();
			int h2b = s.getRowsClearedThisTurn();
			int h3 = s.getRowTransitions();
			int h4 = s.getColTransitions();
			int h5 = s.getNoOfHoles();
			int h6 = s.getWellSum();
			
			double h = w[0] * h1 + w[1] * h2a
					+ w[2] * h2b + w[3] * h3 
					+ w[4] * h4 + w[5] * h5 
					+ w[6] * h6;
			
//			double h = w[0] * h1 + w[1] * h2
//					+ w[2] * h3 + w[3] * h4
//					+ w[4] * h5 + w[5] * h6;

		
			return h;
			

//			float avgH = 0;
//			int highest = 0;
//			int linesCleared = s.getRowsClearedThisTurn();
//			int holes = 0;
//			int blockages = 0;
//			
//			
//			
//			for (int c=9; c<=0; c--) {
//				boolean blocked = false; // # blocks, can contain any number of holes
//				boolean holed = false; // each hole where the top is filled;
//
//				for (int r=19; r<=0; r--) {
//					if (!blocked) {
//						if (stateField[r][c] != 0) {
//							blocked = !blocked;
//						}
//					} else {
//						if (stateField[r][c] == 0) {
//							blockages++;
//							blocked = !blocked;
//						} 
//					}
//
//					if (!holed) {
//						if (stateField[r][c] != 0) {
//							holed = !holed;
//						}
//					} else {
//						if (stateField[r][c] == 0) {
//							holes++;
//						}
//					}
//				}
//			}
//
//
//
//			int[] columns = s.getTop();
//			for (int i=0; i<10; i++) {
//				avgH += columns[i];
//
//				if (columns[i] > highest) {
//					highest = columns[i]; 
//				}
//			}
//			avgH = (float) (avgH/10.0);
//			
//			double totalValue = w[0]*avgH + w[1]*highest
//					+ w[2]*linesCleared + w[3]*holes + w[4]*blockages;
//
//			return totalValue;
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
		// a simple crossover.

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
				
				// 2% mutation --> invert random bits
				float randf = rand.nextFloat(); 
				while (randf < 0.02) {
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
			int randint3 = rand.nextInt(keepcounter);

			newPop[j] = recombination(population[randint1], population[randint2], population[randint3]);
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
		
		
		int numPop = 40; // #games each iteration
		int numIterations = 20;
		float[][] ownPop = {{0.40644622f, 0.40153265f, 0.35011196f, -0.111595646f, 0.13256729f, 0.2923956f, 0.30810428f}, {-0.2939005f, 0.22542453f, 0.35989058f, -0.43820286f, 0.39103866f, -0.34384155f, 0.30404723f}, {0.15324143f, 0.28781778f, 0.73198843f, -0.16860521f, 0.01459044f, -0.028044716f, -0.43691826f}, {0.30111033f, 0.3868304f, -0.4049976f, -0.014205176f, -0.01403421f, 0.018217325f, -0.3116207f}, {-0.17820144f, 0.3404889f, 0.48707736f, -0.46115398f, 0.26583982f, 0.47329903f, -0.2956406f}, {-0.15128118f, 0.43033403f, 0.42853808f, -0.12330708f, 0.4395001f, -0.47983265f, 0.42927033f}, {-0.4152975f, 0.45569766f, -0.23374015f, 0.06676653f, 0.43877953f, 0.3549099f, 0.38235885f}, {-0.40816766f, 0.4498787f, 0.41385698f, -0.015360855f, 0.4464026f, 0.104118824f, -0.42557716f}, {0.23597446f, 0.47276843f, -0.4282956f, -0.89130116f, 2.4609751E-20f, -0.019767895f, -0.277308f}, {-0.21109468f, 0.38342965f, -0.018973954f, 0.014483638f, -0.459157f, -1.661284f, -0.4257102f}, {-0.3253265f, 0.2677322f, 0.42823672f, 0.013690116f, 2.3869343E-20f, -1.5365143f, -0.43322062f}, {-0.15325287f, 0.38347423f, -0.030567572f, -0.20074052f, 0.46104902f, -0.26077867f, -0.43303573f}, {-0.29817492f, 0.38738018f, 0.3175168f, -0.3487457f, 0.32347655f, 0.08077383f, -0.30368638f}, {-0.15446126f, 0.47580677f, -0.36671138f, -0.014525641f, -0.020888682f, -0.44639707f, -0.31087685f}, {-0.1744459f, 0.3365965f, 0.019459486f, 0.2187984f, 2.3725569E-20f, -0.022146285f, -0.30832934f}, {0.31225348f, 0.2906568f, 0.499573f, -0.010382779f, -0.32539105f, 8.273631E36f, -0.4373697f}, {-0.18261674f, 0.4578138f, -0.3561523f, 0.36837852f, -0.3931787f, 0.3473854f, -0.42964518f}, {-0.12540221f, 0.39908576f, -0.19139606f, 0.058498904f, 0.38304257f, 0.40923226f, 0.019474674f}, {-0.20523629f, 0.40297425f, 0.008861482f, -0.43437862f, 0.028569542f, 0.010052811f, -0.4877249f}, {-0.3003716f, 0.25847417f, -0.2147541f, -0.09938988f, 0.34092355f, -1.8529663f, -0.3123659f}, {0.23906782f, 0.47366858f, 0.36384416f, 0.010634437f, 0.37902576f, -6.7136855E36f, 0.30749464f}, {0.15129638f, 0.44987744f, 0.296942f, 0.017116547f, -0.2539724f, -0.4799528f, 0.3113985f}, {-0.42675382f, 0.4166392f, -0.3467189f, -0.028712712f, 0.21898896f, 0.35372925f, -0.2042551f}, {0.14911571f, 0.45180964f, 0.4136138f, -1.076117f, -0.47863322f, 0.4066012f, 0.4021628f}, {-0.12977567f, 0.44495344f, 0.49859762f, 0.023612328f, -0.39086682f, 0.44919395f, 0.42013198f}, {-0.41535234f, 0.20013651f, 0.4888047f, -0.010352757f, 9.1351464E-4f, 0.065331995f, 0.49876094f}, {0.29860622f, 0.41859692f, 0.34229708f, -0.028647676f, 0.3995219f, 1.0462303f, -0.3033545f}, {-0.39826357f, 0.1328781f, -0.28887987f, -0.015555721f, -0.25017285f, -0.029680312f, 0.49213284f}, {-0.29419112f, 0.453386f, 0.41163313f, 0.23295593f, -2.1290653E-20f, -0.31619263f, 0.019436702f}, {-0.4300018f, 0.3756141f, -0.27101314f, 0.082330644f, -0.46526426f, -0.030233681f, 0.42554355f}, {0.27619362f, 0.4616047f, 0.4751706f, -0.12602288f, 0.38399488f, 0.3239441f, 0.019450933f}, {-0.1733245f, 0.33292162f, 0.42822325f, 0.47873092f, -0.337636f, -0.029456258f, -0.2837628f}, {-0.14304808f, 0.20685464f, 0.42078924f, 0.021002017f, -0.16625452f, 0.47702503f, 0.307101f}, {-0.28766692f, 0.36881214f, 0.23822784f, -0.3399403f, 0.3412416f, 1.9628296f, 0.30455387f}, {-0.2123392f, 0.19271341f, -0.28427505f, -1.716764f, -0.17980796f, 0.022367604f, -0.4163953f}, {0.36422443f, 0.13732019f, -0.42532015f, -1.318264f, 0.012143664f, -0.4513893f, -0.2964821f}, {-0.1467036f, 1.0456686E38f, -0.4432149f, 0.014254641f, 0.3830465f, 0.10433352f, 0.027300596f}, {-0.14712942f, 0.4176383f, 0.3807187f, -0.0142547f, 0.3205465f, 0.09621537f, 0.027298689f}, {0.27198678f, 0.22591671f, -0.029701874f, 0.28231943f, -0.27362943f, 0.25986147f, -0.4361329f}, {-0.18225661f, 0.43020797f, 0.030243486f, 0.20275187f, -0.26572895f, -0.35403693f, 0.3074782f}};

//		float[][] popu = new float[numPop][5];

//		GAedit p = new GAedit(numPop);
		GAedit p = new GAedit(ownPop);

		// iterate times to update the population
		for (int j=0; j<numIterations; j++) {
			
			// for each weight, play the game.
			for (int i=0; i<GAedit.popNum; i++) {
				p.setWeight(GAedit.population[i]);
				State s = new State();
				new TFrame(s);

				while(!s.hasLost()) {
					s.makeMove(p.pickMove(s,s.legalMoves()));
					s.draw();
					s.drawNext(0,0);
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

