import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * Extends {@link fitter} in order to use the SVD approximation to solve least
 * squares model for fitting
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías
 *         Gordaliza</a>
 * 
 */
public class GammaFitterSVD extends fitter {
	/* Original data to be fitted */
	// private double[] tAxis;
	// private double[] contAxis;

	/* # Curve points */
	// private int dim;

	/* Fitted data */
	// private double[] fittedCont;

	// Parameters to be obtained(normally) or given by the user.
	private double K, alfa, beta;

	/* window parameters */
	// private int t0,te; //

	/**
	 * Constructor Implementation Initially with the original curve data
	 * 
	 * @param t
	 * @param cont
	 */
	public GammaFitterSVD(double[] t, double[] cont) {
		tAxis = t;
		contAxis = cont;
		dim = t.length;
		fittedCont = new double[dim];
	}

	/**
	 * Creates the GammaFitter setting {@link fitter#contAxis},
	 * {@link fitter#t0}, {@link fitter#te}
	 * 
	 * @param cont
	 */
	public GammaFitterSVD(double[] cont) {
		dim = cont.length;
		tAxis = new double[dim];
		for (int i = 0; i < dim; i++)
			tAxis[i] = i;

		contAxis = cont;
		t0 = MathUtils.minL(contAxis);
		te = MathUtils.minR(contAxis);
		fittedCont = new double[dim];
	}

	/**
	 * @param cont
	 * @param t0
	 * @param te
	 */
	public GammaFitterSVD(double[] cont, int t0, int te) {
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
	public GammaFitterSVD() {

	}

	/**
	 * Calculates the {@link fitter#t0}
	 * 
	 * @param increase
	 *            Minimum distance from the maximum in {@link fitter#contAxis}
	 */
	public void calcT0(int increase) {
		t0 = -1; // EXCEPCIÓN CUANDO ESTO PASE

		try {
			int tMax = MathUtils.whereIs(contAxis, StatUtils.max(contAxis));
			boolean locatedT0 = false;
			int ti = 0;

			while (locatedT0 == false && ti < dim - increase) {
				int inc = 0;
				while (inc < increase
						&& contAxis[ti + inc] < contAxis[ti + inc + 1]) {
					inc++;
				}
				// TODO falta una condición
				if (inc == increase && ti + increase < tMax && contAxis[ti] > 0) {
					locatedT0 = true;
					if ((contAxis[ti - 1] - contAxis[ti]) / contAxis[ti] >= 1)
						t0 = ti;
					else
						t0 = ti + 1;

				}

				ti++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			t0 = -1;
		}

	}

	/**
	 * Calculates the {@link fitter#te}
	 * 
	 * @param increase
	 *            Minimum distance from the maximum in {@link fitter#contAxis}
	 */
	public void calcT(int increase) {
		te = -1;
		double max = StatUtils.max(contAxis);
		int tMax = MathUtils.whereIs(contAxis, max);
		boolean locatedT0 = false;
		// int ti=tMax+1+increase;
		int ti = tMax + 1;
		try {
			while (locatedT0 == false) {
				int inc = 0;
				while (inc < increase
						&& contAxis[ti - inc] < contAxis[ti - inc - 1])
					inc++;

				// TODO falta una condición
				if ((inc == increase || contAxis[ti] < 0.5 * max)) {
					locatedT0 = true;
					while (ti < contAxis.length - 1
							&& contAxis[ti] > contAxis[ti + 1])
						ti++;
				}
				ti++;
			}
			if (ti < contAxis.length)
				te = ti - 1;
			if (FastMath.abs(contAxis[te - 1] / contAxis[te]) < 1.5)
				te--;
		} catch (ArrayIndexOutOfBoundsException e) {
			te = -1;
		}

	}

	public boolean fitting() {

		boolean fitted;
		// TODO
		// EXCEPCION CON T0 NO ENCONTRADO
		if (t0 > 0 && te > 0) {
			RealMatrix X = XMatrix();
			RealVector Y = YVector();
			RealVector B = leastSquareSVD(X, Y);
			setParam(B);
			/*
			 * double[] a = {K,alfa,beta}; myGammaFun mgf = new myGammaFun(2);
			 */

			for (int i = 0; i < dim; i++)
				if (i > t0 && i < dim)
					fittedCont[i] = (K * FastMath.pow((tAxis[i] - t0), alfa))
							* FastMath.exp(-((tAxis[i] - t0) / beta));

			fitted = true;

		} else {
			fittedCont = null;
			fitted = false;
		}
		return fitted;
	}

	

	/*
	 * The t axis must be linearized as... | y1 | | 1 X11 X12| | | | y2
	 * | | 1 X21 X22| | b0 | | . | | . . . | | b1 | | . | = | . . . | | b2 | | .
	 * | = | . . . | | | | yi | | 1 Xi1 Xi2| | |
	 */
	// t0 must be bigger than 0
	private RealMatrix XMatrix() {
		if (te - t0 <= 0)
			System.out.println();
		double[][] coeff = new double[te - t0][3];

		for (int i = 0; i < te - t0; i++) {

			double var = tAxis[t0 + i + 1] - t0;
			coeff[i][0] = 1;
			coeff[i][1] = Math.log(var);
			coeff[i][2] = var;
		}
		return new Array2DRowRealMatrix(coeff);
	}

	/* The cont axis must be linearized as ln(cont) */
	private RealVector YVector() {
		double y[] = new double[te - t0];
		for (int i = 0; i < te - t0; i++)
			if (contAxis[t0 + i + 1] > 0)
				y[i] = Math.log(contAxis[t0 + i + 1]);

		return new ArrayRealVector(y, false);

	}

	
	/**
	 * Least squares resolution by SVD (optimum) using
	 * org.apache.commons.math3.linear
	 * 
	 * @param coefficients
	 * @param constants
	 * @return {@link fitter#contAxis} fitted
	 */
	public static RealVector leastSquareSVD(RealMatrix coefficients,
			RealVector constants) {
		return new SingularValueDecomposition(coefficients).getSolver().solve(
				constants);

	}

	/*
	 * After getting the LSSVD solution is able to set the fitting parameters K
	 * = exp(b0) alfa = b1 beta = -1/b2
	 */

	private void setParam(RealVector sol) {
		K = Math.exp(sol.getEntry(0));
		alfa = sol.getEntry(1);
		beta = -(1 / sol.getEntry(2));
	}

	/**
	 * @return the parameters:[ {@link #K}, {@link #alfa}, {@link #beta}, {@link #t0} ]
	 */
	/* Return the parameters in a double[K,alfa,beta,t0] */
	public double[] getParams() {
		return new double[] { K, alfa, beta, t0 };
	}

	public double[] getFit() {
		return fittedCont;
	}

}
