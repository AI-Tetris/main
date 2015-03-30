import net.sourceforge.jswarm_pso.Swarm;

public class PSO {

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves, double[] coeff) {
		TempPlayer temp = new TempPlayer(s, legalMoves, coeff);
		System.out.println();
		return temp.getMove();
	}

	public static int getLinesCleared(double[] coeff) {
		State s = new State();
		new TFrame(s);
		PSO p = new PSO();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves(), coeff));
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
		return s.getRowsCleared();
	}

	public static void main(String args[]) {
		Swarm swarm = new Swarm(10, new MyParticle(), new MyFitnessFunction());
		swarm.setMaxPosition(new double[] { 0, 10, 0, 0, 0, 0 });
		swarm.setMinPosition(new double[] { -10, 0, -10, -10, -10, -10 });
		for (int i = 0; i < 5; i++) {
			swarm.evolve();
		}
		System.out.println(swarm.toStringStats());

	}

}
