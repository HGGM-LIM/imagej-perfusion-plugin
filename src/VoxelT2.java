import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

// CAso de nuevos estudios
public class VoxelT2 extends Voxel{
	
	/* Contrast direct from the image applying -ln(S(t)/S0)*/
	public final double[] contrastRaw;
	
	/* Contrast after the chosen fit*/
	protected double[] contrastFitted;
	
	/* Contrast without AIF effect*/
	private double[] contrastEstim;
	
	private double MC;
	private double MMC;
	private double FWHM;
	
	protected int t0,te;
	
	private double CBV;
	private double MTT;

	public VoxelT2(int _x,int _y,int _slice,double[] _tac) {
		super(_x,_y,_slice,_tac);
		contrastRaw = new double[_tac.length];
		contrastFitted = new double[_tac.length];
		setContrastRaw(5,1);
		t0 = MathUtils.minL(contrastRaw);
		te = MathUtils.minR(contrastRaw);
	}
	
	/**
	 * 
	 * @param nFrames
	 *            to consider for the one step m-estimator
	 * @param firstFrame
	 *            , will get the value from firstFrame to firstFrame + nFrames
	 *            -1 (0-based)
	 */
	public void setContrastRaw(int nFrames, int firstFrame) {
		double[] values = new double[nFrames];
		for (int i = 0; i < nFrames; i++)
			values[i] = tac[i + firstFrame];

		double S0 = FastMath.log(MathUtils.mEsti(values));

		/* This just happens without a proper mask */
		if (S0 != Double.NEGATIVE_INFINITY && S0 != Double.POSITIVE_INFINITY)
			for (int i = 0; i < tac.length; i++) {
				//if (tac[i] > 0 && FastMath.log(tac[i]) <= S0 )
					if (tac[i] > 0 )
					contrastRaw[i] = -(FastMath.log(tac[i]) - S0);
					else contrastRaw[i] = S0;
		
			}
		
	}
	
	public void setParams() {
		// TODO si contrastRaw no esta inicializada
		double[] parameters = MathAIF.parameters(contrastFitted);
		setMC(parameters[0]);
		setMMC(parameters[1]);
		setFWHM(parameters[2]);

	}
	
	public double getFWHM() {
		return FWHM;
	}

	public void setFWHM(double fWHM) {
		FWHM = fWHM;
	}

	public double getMC() {
		return MC;
	}

	public void setMC(double mC) {
		MC = mC;
	}

	public double getMMC() {
		return MMC;
	}
	public void setMMC(double mMC) {
		MMC = mMC;
	}
	public double getCBV() {
		return CBV;
	}



	public void setCBV(double aifInt) {
		CBV = MathUtils.getCBV(contrastRaw, aifInt);
	}
	
	
	public double[] getContrastRaw() {
		return contrastRaw;
	}
	public void setContrastFitted (fitter f) {
		f.setup(contrastRaw, t0, te);
		f.fitting();
		contrastFitted = f.getFit();
	}
	public double[] getContrastFitted() {
		return contrastFitted;
	}
	public void setContrastFitted(double[] contrastFitted) {
		this.contrastFitted = contrastFitted;
	}
	public void setContrastEstim (double paif[][]) {
		
		contrastEstim = MathUtils.multi(paif, contrastFitted);
	}
	
	public double[] getContrastEstim() {
		return contrastEstim;
	}
	
	public double getMTT() {
		return MTT;
	}
	public void setMMT() {
		double max=StatUtils.max(contrastEstim);
		MTT = StatUtils.sum(contrastEstim)/max;
	}
	
	public boolean isNoisy(double k) {
		
		//double min = StatUtils.min(contrastRaw);
		double minLoc = StatUtils.min(contrastRaw, (int) MMC, (int) (contrastRaw.length - MMC));
		/*if (Double.compare(min, 0) != 0) {
		double param = 1 - k*minLoc/min;
		if (min < 0 && minLoc > 0){
			return  param >2;
		} else 
			return param < 0 ;
		} else
			return minLoc*k >= min;*/
		return FastMath.abs(minLoc) > StatUtils.max(contrastRaw) * k;
			
		
		
		
		
	}



}
