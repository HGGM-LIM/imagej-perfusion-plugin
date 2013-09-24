/**
 * Provides a base class to implement fitting models. Direct Known subclasses:
 * {@link GammaFitterACM}, {@link GammaFitterSVD}, {@link NoFitter}
 * 
 * A fitter needs the values to be fitted, {@link #contAxis} and to set which
 * ones might be used for fitting. From {@link #t0} to {@link #te}
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías
 *         Gordaliza</a>
 * 
 */
public abstract class fitter {

	protected double[] contAxis, tAxis;
	protected int t0, te;
	protected int dim;

	protected double[] fittedCont;

	/**
	 * Implements the fit model
	 * 
	 * @return true if is possible to fit {@link #contAxis}
	 */
	public abstract boolean fitting();

	/**
	 * @return The fitted values
	 */
	public abstract double[] getFit();

	/**
	 * Initializes the {@link fitter}
	 * 
	 * @param contAxis
	 *            The values to be fitted
	 * @param t0
	 *            The
	 * @param te
	 */
	public void setup(double[] contAxis, int t0, int te) {
		setContAxis(contAxis);
		setT0(t0);
		setTe(te);
	}

	/**
	 * Establishes the values {@link #contAxis} to be fitted
	 * @param contAxis
	 */
	public void setContAxis(double[] contAxis) {
		this.contAxis = contAxis;
		dim = contAxis.length;
		fittedCont = new double[contAxis.length];
		tAxis = new double[contAxis.length];
		for (int i = 0; i < tAxis.length; i++)
			tAxis[i] = i;

	}

	/**
	 * 
	 * @param t0
	 */
	public void setT0(int t0) {
		this.t0 = t0;
	}

	/**
	 * @param te
	 */
	public void setTe(int te) {
		this.te = te;
	}

}
