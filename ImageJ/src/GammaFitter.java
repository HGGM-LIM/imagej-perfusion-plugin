
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class GammaFitter extends fitter {
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

	/*
	 * Constructor Implementation Initially with the original curve data
	 */
	public GammaFitter(double[] t, double[] cont) {
		tAxis = t;
		contAxis = cont;
		dim = t.length;
		fittedCont = new double[dim];
	}

	public GammaFitter(double[] cont) {
		dim = cont.length;
		tAxis = new double[dim];
		for (int i = 0; i < dim; i++)
			tAxis[i] = i;

		contAxis = cont;
		t0 = MathUtils.minL(contAxis);
		te = MathUtils.minR(contAxis);
		fittedCont = new double[dim];
	}

	public GammaFitter(double[] cont, int t0, int te) {
		dim = cont.length;
		tAxis = new double[dim];
		for (int i = 0; i < dim; i++)
			tAxis[i] = i;

		contAxis = cont;
		fittedCont = new double[dim];
		this.t0 = t0;
		this.te = te;
	}

	public GammaFitter() {

	}

	// MEJORAS EN LA BUSQUEDA DADO QUE PODRÍA REPETIR VALORES YA DESCARTADOS
	public void calcT0(int increase) {
		t0 = -1; // EXCEPCIÓN CUANDO ESTO PASE
		// TODO CUANDO te pasas del máximo
		// int tMax = new ArrayRealVector(contAxis).getMaxIndex();
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
					/*
					 * if ( (contAxis[ti-1] - contAxis[ti]) / contAxis[ti] >= 1)
					 * te=ti; else te = ti-1;
					 */

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

	public void calcT1(int increase) {
		t0 = -1;
		double max = StatUtils.max(contAxis);
		int tMax = MathUtils.whereIs(contAxis, max);
		boolean locatedT0 = false;
		int ti = tMax - 1;
		try {
			while (locatedT0 == false && ti > increase) {
				int inc = 0;
				while (inc < increase
						&& contAxis[ti - inc] > contAxis[ti - inc - 1])
					inc++;

				// TODO falta una condición
				if ((inc == increase || contAxis[ti] < 0.5 * max)) {
					locatedT0 = true;
					while (ti > 0 && contAxis[ti] > contAxis[ti - 1])
						ti--;

					/*
					 * if ( (contAxis[ti-1] - contAxis[ti]) / contAxis[ti] >= 1)
					 * t0=ti; else t0 = ti+1;
					 */

				}
				ti--;
			}
			t0 = ti + 1;
		} catch (ArrayIndexOutOfBoundsException e) {
			t0 = -1;
		}

	}

	public void calcTe(int MMC) {
		te = MMC + 1;
		try {
			while (contAxis[te] >= 0.1)
				if (contAxis[te] <= contAxis[te - 1]
						|| contAxis[te + 1] <= contAxis[te])
					te++;
				else
					te = -1;
		} catch (ArrayIndexOutOfBoundsException e) {
			te = -1;
		}
	}

	/*
	 * bla bla bla bla bla bla B=pinv(X)·Y
	 */
	public boolean fitting() {
		// t0 = MathUtils.minL(contAxis);
		// te = MathUtils.minR(contAxis);
		boolean fitted;
		// TODO
		// EXCEPCION CON T0 NO ENCONTRADO
		if (t0 > 0 && te > 0) {
			RealMatrix X = XMatrix();
			RealVector Y = YVector();
			RealVector B = leastSquareSVD(X, Y);
			setParam(B);

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

	/* Parameters are given by the user */
	public void fitting(boolean parameters) {

	}

	/*
	 * The t axis must be linearized as...COMPLETAR | y1 | | 1 X11 X12| | | | y2
	 * | | 1 X21 X22| | b0 | | . | | . . . | | b1 | | . | = | . . . | | b2 | | .
	 * | = | . . . | | | | yi | | 1 Xi1 Xi2| | |
	 */
	// TODO "t" TIENE QUE SER MAYOR QUE "t0" !!OjO!!
	public RealMatrix XMatrix() {
		if (te - t0 <= 0)
			System.out.println();
		double[][] coeff = new double[te - t0][3];

		for (int i = 0; i < te - t0; i++) {
			// obligo ti > 0

			double var = tAxis[t0 + i + 1] - t0;
			coeff[i][0] = 1;
			coeff[i][1] = Math.log(var);
			coeff[i][2] = var;
		}
		return new Array2DRowRealMatrix(coeff);
	}

	/* The cont axis must be linearized as ln(cont) */
	public RealVector YVector() {
		double y[] = new double[te - t0];
		for (int i = 0; i < te - t0; i++)
			if (contAxis[t0 + i + 1] > 0)
				y[i] = Math.log(contAxis[t0 + i + 1]);

		return new ArrayRealVector(y, false);

	}

	/*
	 * Least squares resolution by SVD (optimum) using
	 * org.apache.commons.math3.linear
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

	/* Return the parameters in a double[K,alfa,beta,t0] */
	public double[] getParams() {
		return new double[] { K, alfa, beta, t0 };
	}

	public double[] getFit() {
		return fittedCont;
	}

}
