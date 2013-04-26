import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

// CAso de nuevos estudios
public class VoxelT2 extends Voxel{
	
	/* Contrast direct from the image applying -ln(S(t)/S0)*/
	protected double[] contrastRaw;
	
	/* Contrast after the chosen fit*/
	protected double[] contrastFitted;
	
	/* Contrast without AIF effect*/
	private double[] contrastEstim;
	
	/* Curve parameters*/
	/* Maximum Concentration*/
	private double MC;
	/* Moment Maximum Concetration*/
	private double MMC;
	/* Full Width Half Maximum */
	private double FWHM;
	
	/* Window times. Content valid values ig is possible to fit the curve */
	protected int t0,te;
	
	/* True if the voxel is used for AIF calculataion*/
	protected boolean AIFValid;
	
	/* Parameters to study*/
	/* Cerebral Blood Flow*/
	private double CBV;
	/* Mean Transit Time*/
	private double MTT;
	/* Cerebral Blood flow*/
	private double CBF;

	/**
	 * Single constructor for this kind of Voxel
	 * @param _x x-axis where the voxel is placed in the image
	 * @param _y y-axis where the voxel is placed
	 * @param _slice the specific brain slice 
	 * @param _tac time signal evolution for the voxel
	 */
	public VoxelT2(int _x,int _y,int _slice,double[] _tac) {
		super(_x,_y,_slice,_tac);
		contrastRaw = new double[_tac.length];
		contrastFitted = new double[_tac.length];
		setContrastRaw(5,1);
		//contrastRaw = MathUtils.vecSum(contrastRaw, FastMath.abs(StatUtils.min(contrastRaw)));
		//t0 = MathUtils.chinL(contrastRaw, 6);
		//te = MathUtils.chinR(contrastRaw, );
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
		//contrastRaw = MathUtils.vecSum(contrastRaw, FastMath.abs(StatUtils.min(contrastRaw)));
		
	}
	
	/**
	 * Get and establish the curve parameters(MC,MMC,FWHM) from the Fitted contrast
	 */
	public void setParams() {
		// TODO si contrastRaw no esta inicializada
		double[] parameters = MathAIF.parameters(contrastFitted);
		setMC(parameters[0]);
		setMMC(parameters[1]);
		setFWHM(parameters[2]);

	}
	
	/**
	 * FWHM getter
	 * @return FWHM's curve
	 */
	public double getFWHM() {
		return FWHM;
	}

	/**
	 * FWHM setter
	 * @param fWHM
	 */
	public void setFWHM(double fWHM) {
		FWHM = fWHM;
	}

	/**
	 * MC getter
	 * @return MC's curve
	 */
	public double getMC() {
		return MC;
	}

	/**
	 * MC setter
	 * @param mC
	 */
	public void setMC(double mC) {
		MC = mC;
	}

	/**
	 * MMC getter
	 * @return MMC
	 */
	public double getMMC() {
		return MMC;
	}
	
	/**
	 * MMC setter
	 * @param mMC
	 */
	public void setMMC(double mMC) {
		MMC = mMC;
	}
	
	/**
	 * CBV getter
	 * @return CBV
	 */
	public double getCBV() {
		return CBV;
	}


	/**
	 * Establish the CBV from the Eq. CBV = S(C)/S(AIF)
	 * where S is the integral, C is the contrast direct 
	 * from the signal
	 * @param aifInt  AIF function integrated
	 */
	public void setCBV(double aifInt) {
		CBV = MathUtils.interBad(contrastRaw)/ aifInt;
	}
	
	/**
	 * 
	 * @return Direct contrast from the signal
	 */
	public double[] getContrastRaw() {
		return contrastRaw;
	}
	
	/**
	 * Establish the contrast fitted 
	 * @f kind of fitter
	 */
	public void setContrastFitted (fitter f) {
		f.setup(contrastRaw, t0, te);
		f.fitting();
		contrastFitted = f.getFit();
	}
	
	/**
	 * Contrast fitted getter
	 * @return contrastFitted
	 */
	public double[] getContrastFitted() {
		return contrastFitted;
	}
	
	/**
	 * contrastFitted setter
	 * @param contrastFitted
	 */
	public void setContrastFitted(double[] contrastFitted) {
		this.contrastFitted = contrastFitted;
	}
	
	/**
	 * Establish the contrast without AIF influence
	 * based on the deconvolution Eq (paper...) 
	 * @paif pseudo-inverse matrix from the low-triangular aif
	 */
	public void setContrastEstim (double paif[][]) {
		
		contrastEstim = MathUtils.multi(paif, contrastFitted);
	}
	
	public double[] getContrastEstim() {
		return contrastEstim;
	}
	
	public double getMTT() {
		return MTT;
	}
	
	/**
	 * Establish the MTT
	 */
	public void setMMT() {
		double max=StatUtils.max(contrastEstim);
		MTT = MathUtils.interBad(contrastEstim)/max;
	}
	
	/**
	 * 
	 * @param k threshold 
	 * @return true if the contrastRaw decays after the spike to a level at least k
	 * times the maximun 
	 */
	public boolean isNoisy(double k) {
		
		//double min = StatUtils.min(contrastRaw);
	
		double minLoc = StatUtils.min(contrastRaw, (int) MMC, (int) (contrastRaw.length - MMC));
		//double minLoc = StatUtils.min(contrastRaw, 0,40);
		return FastMath.abs(minLoc) > StatUtils.max(contrastRaw) * k;
	}
	
	public boolean isNoise() {
		if (StrictMath.abs(StatUtils.min(contrastRaw)) >= StatUtils.max(contrastRaw))
			return true;
		return false;
	}



}
