import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;


/**
 * Extends {@link fitter} in order to use the curve fitter provided by {@link CurveFitter}
 *  for getting the values fitted to a Gamma function
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías Gordaliza</a>
 *
 */
public class GammaFitterACM extends fitter {
	
	/**
	 * Creates the {@link fitter} with the required parameters 
	 * @param cont
	 * @param t0
	 * @param te
	 */
	public GammaFitterACM(double[] cont, int t0, int te) {
		dim = cont.length;
		tAxis = new double[dim];
		for (int i = 0; i < dim; i++)
			tAxis[i] = i;

		contAxis = cont;
		fittedCont = new double[dim];
		this.t0 = t0;
		this.te = te;
	}
	/**
	 * 
	 */
	public GammaFitterACM() {
		
	}


	public boolean fitting() {
		
		if(t0 > 0 && te > 0 ) {
			/* Needs to implement linearGamma*/
		CurveFitter<linearGamma> fitt = new CurveFitter<linearGamma>(new LevenbergMarquardtOptimizer());
		intPoin(fitt);
		double[] best;
		double[] ord ={1,1,1};
		double[] param;
		try {
		 best=fitt.fit(new linearGamma(t0),ord);
		 param = setParam(best);
		} catch (Exception e) {
			fittedCont=null;
			return false;
		}
		
		myGammaFun mgf = new myGammaFun(t0);
		
		for (int i=0; i < fittedCont.length; i++)
			fittedCont[i] = mgf.value(i, param);
		
		return true;
		} else 
		fittedCont=null;
		return false;
	}

	
	public double[] getFit() {
		return fittedCont;
	}
	
	/* Fills the CurveFitter*/
	private void intPoin(CurveFitter<linearGamma> fitter) {
		
		for(int i=t0+1; i <= te ; i++) 
			if(contAxis[i] > 0)
			fitter.addObservedPoint(i, FastMath.log(contAxis[i]));
			else 
				//System.out.println();
				fitter.addObservedPoint(i,-5*StatUtils.max(contAxis));
	}
	
	/* Get the real gamma parameters from the linear ones */
	private double[] setParam(double[] sol) {
		double []result=new double[3];
		result[0] = Math.exp(sol[0]);
		result[1] = sol[1];
		result[2] = -(1 / sol[2]);
		return result;
	}

}
