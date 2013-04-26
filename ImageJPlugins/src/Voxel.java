

public class Voxel {

	/* X coordinate */
	public final int x;

	/* Y coordinate */
	public final int y;

	/* Z coordinate,actually the slice */
	public final int slice;

	/* Time function contrast */

	public final double[] tac;
	
	

	/* Constructor */
	public Voxel(int _x, int _y, int _slice, double[] _tac) {
		x = _x;
		y = _y;
		slice = _slice;
		tac = _tac;

	}


	public void setContrastRaw(int nFrames, int firstFrame) {}

	
	public void setContrastEstim (double aif[][]) {}
	
	public void setContrastFitted(fitter f){}

	public void setParams() {}
	
	
	

}
