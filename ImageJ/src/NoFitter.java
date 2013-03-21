
public class NoFitter extends fitter {
	//double[] contAxis;

	public NoFitter(double[] contAxis,int t0,int te) {
		// TODO Auto-generated constructor stub
		this.contAxis = contAxis;
		fittedCont = contAxis;
	}
	public NoFitter(){}

	@Override
	public boolean fitting() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public double[] getFit() {
		// TODO Auto-generated method stub
		return contAxis;
	}

}
