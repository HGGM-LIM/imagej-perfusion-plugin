
/**
 * Implements the case without fitting the data
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías Gordaliza</a>
 *
 */
public class NoFitter extends fitter {


	/**
	 * @param contAxis
	 * @param t0
	 * @param te
	 */
	public NoFitter(double[] contAxis,int t0,int te) {
	
		this.contAxis = contAxis;
		fittedCont = contAxis;
	}
	/**
	 * 
	 */
	public NoFitter(){}

	public boolean fitting() {
		return true;
	}

	public double[] getFit() {
		return contAxis;
	}

}
