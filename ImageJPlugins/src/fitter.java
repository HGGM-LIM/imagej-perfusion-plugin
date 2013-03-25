
public abstract class fitter {
	
	protected double[] contAxis,tAxis;
	protected int t0,te;
	protected int dim;
	
	protected double[] fittedCont;
	
	
	public abstract boolean fitting();
	public abstract double[] getFit();
	
	
	public void setup(double[] contAxis,int t0,int te) {
		setContAxis(contAxis);
		setT0(t0);
		setTe(te);
	}
	
	public void setContAxis(double []contAxis) {
		this.contAxis = contAxis;
		dim = contAxis.length;
		fittedCont = new double[contAxis.length];
		tAxis = new double[contAxis.length];
	    for (int i=0; i < tAxis.length; i++)
	    	tAxis[i] = i;
	    	
	}
	
	public void setT0(int t0) {
		this.t0 = t0;
	}
	
	public void setTe(int te) {
		this.te = te;
	}
	
	

}
