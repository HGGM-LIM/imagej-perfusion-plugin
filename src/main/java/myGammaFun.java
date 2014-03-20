import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.util.FastMath;


/**
 * Implements the gamma function K(t -t0)^alfa*exp((t-t0)/beta)
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías Gordaliza</a>
 *
 */
public class myGammaFun implements ParametricUnivariateFunction {
	private int t0;

	/**
	 * @param t0
	 */
	public myGammaFun(int t0) {
		this.t0 = t0;
	}

	
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
	
	public double value(double arg0, double... arg1) {
		double k =  arg1[0];
		double alfa = arg1[1];
		double beta = arg1[2];
		double t = arg0 - t0;
		
		if (t > 0)
		return  (k * FastMath.pow((t), alfa))
				* FastMath.exp(-((t) / beta));
		else
			return 0;
	
		
	}
	
	

}
