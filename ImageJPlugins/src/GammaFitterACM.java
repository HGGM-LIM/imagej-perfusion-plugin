import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;


public class GammaFitterACM extends fitter {
	
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
	public GammaFitterACM() {
		
	}

	@Override
	public boolean fitting() {
		// TODO Auto-generated method stub
		if(t0 > 0 && te > 0 ) {
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
		//double[] result = new double[fittedCont.length];
		for (int i=0; i < fittedCont.length; i++)
			fittedCont[i] = mgf.value(i, param);
		
		return true;
		} else 
		fittedCont=null;
		return false;
	}

	@Override
	public double[] getFit() {
		// TODO Auto-generated method stub
		return fittedCont;
	}
	
	private void intPoin(CurveFitter<linearGamma> fitter) {
		//int t0 = MathUtils.minL(vals)-1;
		//int te = MathUtils.minR(vals);
		for(int i=t0+1; i <= te ; i++) 
			if(contAxis[i] > 0)
			fitter.addObservedPoint(i, FastMath.log(contAxis[i]));
			else 
				//System.out.println();
				fitter.addObservedPoint(i,-5*StatUtils.max(contAxis));
	}
	
	private double[] setParam(double[] sol) {
		double []result=new double[3];
		result[0] = Math.exp(sol[0]);
		result[1] = sol[1];
		result[2] = -(1 / sol[2]);
		return result;
	}

}
