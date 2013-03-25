import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.util.FastMath;


public class myGammaFun implements ParametricUnivariateFunction {
	/* params[0] = K;
	 * params[1] = alfa;
	 * params[2] = beta;
	 */
	private double[] params;
	private double t0;

	public myGammaFun(int t0) {
		// TODO Auto-generated constructor stub
		this.t0 = t0;
	}

	@Override
	public double[] gradient(double arg0, double... arg1) {
		// TODO Auto-generated method stub
		double t = arg0 ;
		final double[] gradient = new double[arg1.length];
		double k =  arg1[0];
		double alfa = arg1[1];
		double beta = arg1[2];
		
		gradient[0] = FastMath.pow(t, alfa)*FastMath.exp(-(t/beta));
		gradient[1] = k*FastMath.log(t)*FastMath.pow(t, alfa)*FastMath.exp(-(t/beta));
		gradient[2] = k*FastMath.pow(t, alfa)*FastMath.exp(-(t/beta))*(t/(beta*beta));
		
		
		return gradient;
	}
	//6.885284315579477E-13, 28.760307027334886, 0.25605599110315486]
	@Override
	public double value(double arg0, double... arg1) {
		double k =  arg1[0];
		double alfa = arg1[1];
		double beta = arg1[2];
		double t = arg0 ;
		return  (k * FastMath.pow((t), alfa))
				* FastMath.exp(-((t) / beta));
	
		
	}

}
