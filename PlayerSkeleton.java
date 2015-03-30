public class PlayerSkeleton {

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {

		/*
		 * double[] coeff = { -4.500158825082766, 3.4181268101392694,
		 * -3.2178882868487753, -9.348695305445199, -7.899265427351652,
		 * -3.3855972247263626 };
		 */

		double[] coeff = { -4.253977908308251, 0.0, -0.8598644677923453, -10.0,
				-0.6954944236172037, -4.011013610301695 };

		TempPlayer temp = new TempPlayer(s, legalMoves, coeff);
		System.out.println();
		return temp.getMove();
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
	}

}
