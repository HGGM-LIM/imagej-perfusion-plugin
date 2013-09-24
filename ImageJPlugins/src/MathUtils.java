

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class MathUtils {

	/**
	 * Calculate the one-step M-estimator where the result comes by (1.28 * MADN
	 * * (MU -L) +MB) / (n - L - MU) MADN = MAD / 0.6745 ; MAD = Median (|values
	 * - M|) ; M = Median (values) ; MU = outliers greater than the median L =
	 * number of outliers smaller than the media MU = sum(nonOutliers)
	 * 
	 * @param vals
	 *            are the values whose estimator is required.
	 * @return a specific one step M-estimator
	 */
	public static double mEsti(double[] vals) {
		/* Median is the percentile 50 */
		double M = StatUtils.percentile(vals, 50);
		double MAD = StatUtils.percentile(vecSubs(vals, M, true), 50);
		double MADN = MAD / 0.6745;
		double[] outliers = outliers(vals, M, MAD);
		int MU = 0, L = 0;
		double MB = 0;
		if (outliers.length != 0) {
			MU = amountBigger(outliers, M);
			L = FastMath.abs(MU - outliers.length);
			MB = StatUtils.sum(nonOutliers(vals, outliers));
		} else {
			MB = StatUtils.sum(vals);
		}
		if (MU + L != vals.length)
			return (1.28 * MADN * (MU - L) + MB) / (vals.length - L - MU);
		else
			// TODO Ocasiones en las que el denominador es 0
			return M;

	}

	/* Simple method for subtracting a constant from a double[] */
	public static double[] vecSubs(double[] vec, double subs, boolean abs) {
		double[] result = new double[vec.length];
		for (int i = 0; i < vec.length; i++) {
			if (abs == true)
				result[i] = FastMath.abs(vec[i] - subs);
			else
				result[i] = vec[i] - subs;
		}
		return result;
	}
	
	/**
	 * Sum a constant to a vector
	 * @param vec 
	 * @param sum the constant to be summed 
	 * @return vec + sum
	 */
	public static double[] vecSum(double[] vec,double sum) {
		double[] result = new double[vec.length];
		for (int i = 0; i < vec.length; i++)
			result[i] = vec[i] + sum;
		
		return result;
					
	}

	/* Getting the amount of numbers bigger than a threshold */

	public static int amountBigger(double[] vals, double trheshold) {
		int result = 0;
		for (int i = 0; i < vals.length; i++)
			if (vals[i] > trheshold)
				result++;

		return result;

	}

	public static int amountOnes(boolean[] ind) {
		int result = 0;
		for (int i = 0; i < ind.length; i++)
			if (ind[i] == true)
				result++;

		return result;

	}

	/* Outilers for specific... */
	/*
	 * Could be faster, sorting the numbers is esay to see which ones are
	 * outliers (for big inputs)
	 */
	private static double[] outliers(double[] vals, double med, double mad) {

		double[] result;
		boolean[] ind = new boolean[vals.length];
		double[] aux = vecSubs(vals, med, true);
		int j = 0;
		for (int i = 0; i < aux.length; i++)
			if (aux[i] / (mad * 0.6475) > 1.28) {
				ind[i] = true;
				j++;
			}
		result = new double[j];

		for (int i = 0, k = 0; i < ind.length; i++)
			if (ind[i] == true) {
				result[k] = vals[i];
				k++;
			}
		return result;

	}

	/* Just getting samples */
	private static double[] nonOutliers(double[] vals, double[] outl) {
		double[] result = new double[vals.length - outl.length];
		for (int i = 0, j = 0, k = 0; i < vals.length; i++)
			if (vals[i] != outl[j]) {
				result[k] = vals[i];
				k++;
			} else {
				if (j < outl.length - 1)
					j++;
			}
		return result;
	}

	

	/**
	 * Find a value v into the values
	 * @param vals
	 * @param v
	 * @return the position of v inside vals
	 */
	public static int whereIs(double[] vals, double v) {
		for (int i = 0; i < vals.length; i++)
			if (vals[i] == v)
				return i;

		return -1;

	}

	/**
	 * 
	 * @param values
	 * @param trheshold
	 * @return True where the values are bigger than the threshold
	 */
	public static boolean[] indBiggerThan(double[] values, double trheshold) {
		boolean[] ind = new boolean[values.length];
		for (int i = 0; i < values.length; i++)
			if (values[i] > trheshold)
				ind[i] = true;

		return ind;
	}

	/**
	 * 
	 * @param values
	 * @param trheshold
	 * @return indices where values are bigger than the threshold
	 */
	public static int[] findBiggerThan(double[] values, double trheshold) {
		boolean[] ind = indBiggerThan(values, trheshold);
		int[] result = new int[amountOnes(ind)];
		for (int i = 0, j = 0; i < ind.length; i++)
			if (ind[i] == true) {
				result[j] = i;
				j++;
			}
		return result;
	}

	public static double getCBV(double[] con, double aif) {
		return (interBad(con)) / aif;

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

	public static double[] SVDsol(RealMatrix coefficients, RealVector constants) {
		return new SingularValueDecomposition(coefficients).getSolver()
				.solve(constants).toArray();

	}

	/**
	 * Simple function for getting a lowTriangular matrix by repetition of the
	 * values
	 * 
	 * @param vals
	 *            The values
	 * @return The lowTriangularMatrix
	 */
	public static double[][] lowTriangular(double[] vals) {
		int dim = vals.length;
		double[][] matrix = new double[dim][dim];
		for (int i = 0; i < dim; i++)
			for (int j = 0; (i + 1) / (j + 1) >= 1; j++)
				matrix[i][j] = vals[i - j];

		return matrix;
	}

	/**
	 * Simple function for getting a lowTriangular matrix by repetition of the
	 * values
	 * 
	 * @param vals
	 *            The values
	 * @return The lowTriangularMatrix
	 */
	public static RealMatrix lowTriangularM(double[] vals) {
		return new Array2DRowRealMatrix(lowTriangular(vals));
	}

	/**
	 * Performs the deconvolution operation given by c(t) =
	 * sum[f(tao)*g(t-tao)] where the sum extends tao from -infinity to infinity
	 * (theory) Hence in a matricial way | c0 | | f0 0 0 ··· 0 | | c1 | | f1 f0
	 * 0 ··· 0 | | c2 | = | f2 f1 f0 ··· 0 | | ··· | | ··· ··· ··· ··· 0 | | ct
	 * | | ft ft-1 ft-2··· f0 |
	 * 
	 * @param f
	 * @param g
	 * @return The leastSquare optimal solution for the system
	 */

	public static double[] deConvolLS(double[] f, double[] g) {
		return SVDsol(lowTriangularM(f), new ArrayRealVector(g, false));
	}

	/**
	 * Performs the deconvolution opertation given by c(t) =
	 * sum[f(tao)*g(t-tao)] where the sum extends tao from -infinity to infinity
	 * (theory) Hence in a matricial way | c0 | | f0 0 0 ··· 0 | | c1 | | f1 f0
	 * 0 ··· 0 | | c2 | = | f2 f1 f0 ··· 0 | | ··· | | ··· ··· ··· ··· 0 | | ct
	 * | | ft ft-1 ft-2··· f0 |
	 * 
	 * @param f
	 * @param g
	 * @return The leastSquare optimal solution for the system
	 */
	public static double[] deConvolLS(double[][] f, double[] g) {
		return SVDsol(new Array2DRowRealMatrix(f, false), new ArrayRealVector(
				g, false));
	}

	/**
	 * 
	 * @param n
	 * @param prec
	 * @return the number n rounded with the accuracy prec
	 */
	public static double round(double n, int prec) {
		int p = (int) FastMath.pow(10, prec);
		return FastMath.rint(n * p) / p;
	}

	public static int calcT(double[] contAxis, int increase) {
		int te = -1;
		int tMax = MathUtils.whereIs(contAxis, StatUtils.max(contAxis));
		boolean locatedT0 = false;
		int ti = tMax + 1 + increase;
		while (locatedT0 == false && ti < contAxis.length - 1) {
			int inc = 0;
			while (inc < increase
					&& contAxis[ti - inc] < contAxis[ti - inc - 1])
				inc++;

			// TODO falta una condición
			if (inc == increase && contAxis[ti] > 0) {
				locatedT0 = true;
				while (ti < contAxis.length - 1
						&& contAxis[ti] > contAxis[ti + 1])
					ti++;
				if ((contAxis[ti - 1] - contAxis[ti]) / contAxis[ti] >= 1)
					te = ti;
				else
					te = ti - 1;

			}
			ti++;
		}
		return te;

	}

	/**
	 * 
	 * @param aif low-triangular shifted matrix
	 * @return the aif's pseudo-inverse by SVD
	 */
	public static double[][] pInvMon(double[][] aif) {
		RealMatrix trunUTrans,trunS,trunV;
		SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(aif,
				false));
		int rank = svd.getRank();
		/*double [] sinV = svd.getSingularValues();
				double [] cag = new double[rank];
		for (int i = 0; i < rank; i++)
			cag[i] = sinV[i];*/
		trunS = svd.getS().getSubMatrix(0, rank -1, 0, rank -1);
		trunV = svd.getV().getSubMatrix(0,aif[0].length - 1, 0, rank -1);
		trunUTrans = svd.getUT().getSubMatrix(0,  rank -1, 0, aif[0].length - 1);
		//IJ.showMessage(" " +interBad(cag) / interBad(svd.getSingularValues()));
		return trunV.multiply(trunS).multiply(trunUTrans).getData();
		
		/*return new SingularValueDecomposition(new Array2DRowRealMatrix(aif,
				false)).getSolver().getInverse().getData();*/
	}

	/**
	 * 
	 * @param coef
	 * @param cons
	 * @return coef*cons
	 */
	public static double[] multi(double[][] coef, double[] cons) {
		return new Array2DRowRealMatrix(coef, false).operate(cons);

	}

	/**
	 * 
	 * @param max
	 * @param tMax
	 * @param vals
	 * @return the located min before the spike
	 */
	public static int minL(double max, int tMax, double[] vals) {
		int tMaxRel;
		int ti = tMax - 1;
		boolean located = false;
		try {
			while (located == false) {
				while (vals[ti] >= vals[ti - 1] && vals[ti] > 0
						/*&& (vals[ti] - vals[ti - 1] <= FastMath
						.abs(vals[ti - 1]) * 4)*/)
					ti--;
				tMaxRel = maxLFrom(ti, vals);
				if (((max - vals[ti]) >= 0.5 * max || (vals[ti] - vals[ti - 1] >= vals[ti - 1] * 4))
						&& tMaxRel != -1)
					located = true;
				else if (tMaxRel != -1)
					ti = tMaxRel;
				else
					ti = -1;

			}
			/*
			 * if( vals[ti +1] - vals[ti] >= 0.5*vals[ti +1] && ti + 1 != tMax)
			 * ti++;
			 */

			while (vals[ti + 1] <= 0)
				ti++;

			if (ti == tMax)
				ti = -1;

			return ti;
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}

	/**
	 * 
	 * @param vals
	 * @return the located min before the spike
	 */
	public static int minL(double[] vals) {
		return minL(StatUtils.max(vals),
				MathUtils.whereIs(vals, StatUtils.max(vals)), vals);
	}

	/**
	 * 
	 * @param max
	 * @param tMax
	 * @param vals
	 * @return the located min after the spike
	 */
	public static int minR(double max, int tMax, double[] vals) {

		int tMaxRel;
		int ti = tMax + 1;
		boolean located = false;
		try {
			while (located == false) {
				while (vals[ti] >= vals[ti + 1]
						&& (vals[ti] - vals[ti + 1] <= vals[ti + 1] * 4)
						&& vals[ti + 1] > 0)
					ti++;
				tMaxRel = maxRFrom(ti, vals);
				if ((((vals[tMax] - vals[ti]) >= 0.5 * max) || (vals[ti]
						- vals[ti + 1] >= vals[ti + 1] * 4))
						&& tMaxRel != -1)
					located = true;
				else if (tMaxRel != -1)
					ti = tMaxRel;
				else
					ti = -1;
			}

			/*
			 * if(vals[ti-1] - vals[ti] >= 0.5 *vals[ti-1] ) ti--;
			 */
			while (vals[ti] < 0)
				ti--;

			if (ti == tMax)
				ti = -1;

			return ti;
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}

	/**
	 * 
	 * @param vals
	 * @return  the located min after the spike
	 */
	public static int minR(double[] vals) {
		return minR(StatUtils.max(vals),
				MathUtils.whereIs(vals, StatUtils.max(vals)), vals);
	}

	public static int maxRFrom(int tFrom, double[] vals) {
		/* return -1 means always ascending */
		int tResult = tFrom;
		try {
			while (vals[tResult + 1] >= vals[tResult])
				tResult++;
			return tResult;

		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}

	public static int maxLFrom(int tFrom, double[] vals) {
		/* return -1 means always ascending */
		int tResult = tFrom;
		try {
			while (vals[tResult - 1] >= vals[tResult])
				tResult--;
			return tResult;

		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}
	
	public static int chinL (double[] vals,int ti) {
		int tMax = MathUtils.whereIs(vals,StatUtils.max(vals));
		while(ti < vals.length-3) {
		int i = 0;
		while (i < 3 && vals[ti+i] < vals [ti+i+1])
			i++;
		if (i == 3 && ti + 3 < tMax) {
			if ( (vals[ti +1] / vals[ti]) - 1  >= 1 )
				return ti;
			else 
				return ti + 1;
		} else 
			 ti++;
		}
		return -1;
	}
	
	public static int chinR(double[] vals,int ti) {
		int tMax = MathUtils.whereIs(vals,StatUtils.max(vals));
		while(ti < vals.length -3) {
		int i = 0;
		while (i < 3 && vals[ti+i] > vals[ti+i+1])
			i++;
		if( i==3 && ti > tMax) {
			if ( (vals[ti - 1] / vals[ti]) - 1  >= 1)
				return ti;
			else
				return ti - 1;
		} else
			ti++;
		}
		return -1;
	}
	
	/**
	 * 
	 * @param values
	 * @param k
	 * @return  inf limit and sup limit for identifying outliers
	 */
	public static double[] limitOutl(double[] values,double k) {
		double[]result = new double[2];
		double q1 = StatUtils.percentile(values, 5);
		double q3 = StatUtils.percentile(values, 95);
		result[0] = q1 - k*(q3-q1);
		result[1] = q3 + k*(q3-q1);
		
		return result;
		
	}
	
	public static double[] limitOutlM(double[] values, double k) {
		double[] result = new double[2];
		result[0] = StatUtils.mean(values) - k*FastMath.sqrt(StatUtils.variance(values));
		result[1] = StatUtils.mean(values) + k*FastMath.sqrt(StatUtils.variance(values));
		
		return result;
	}
	
	public static double interBad(double[] values) {
		double result = 0;
		for (int i = 0; i < values.length; i++)
			result += FastMath.abs(values[i]);
		
		return result;
	}
		

}
