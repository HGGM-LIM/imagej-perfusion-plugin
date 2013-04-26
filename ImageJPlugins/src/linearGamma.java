import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.util.FastMath;


public class linearGamma implements ParametricUnivariateFunction {

	int t0;
	public linearGamma(int t0) {
		this.t0 = t0;
	}
	@Override
	public double[] gradient(double arg0, double... arg1) {
		// TODO Auto-generated method stub
		double [] result = new double[arg1.length];
		result[0] = 1;
		result[1] = FastMath.log(arg0 - t0);
		result[2] = arg0-t0;
		return result;
	}

	@Override
	/**
	 * filling --> y = b0 + b1*ln(x) + b2*x
	 */
	public double value(double arg0, double... arg1) {
		// TODO Auto-generated method stub
		return arg1[0] + arg1[1]*FastMath.log(arg0-t0)+arg1[2]*(arg0-t0);
	}

}
