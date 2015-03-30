import net.sourceforge.jswarm_pso.FitnessFunction;

public class MyFitnessFunction extends FitnessFunction {
	public double evaluate(double[] position) {
		return PSO.getLinesCleared(position);
	}
}
