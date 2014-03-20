/**
 * Simple data transfer object to ease the analysis of all the TACs in a given
 * image. Objects returned by the {@link ImagePlusHypIterator} class are of this
 * type.
 * 
 * @author <a href="mailto:jmmateos@mce.hggm.es">José María Mateos</a>.
 * 
 */
public class Voxel {

	/**
	 * X-coordinate for the voxel.
	 */
	public final int x;

	/**
	 * Y-coordinate for the voxel.
	 */
	public final int y;

	/**
	 * Slice (1-based) for the voxel.
	 */
	public final int slice;

	/**
	 * Time-activity curve.
	 */

	public final double[] tac;

	/**
	 * Main constructor for this data transfer object
	 * 
	 * @param x
	 *            X-coordinate.
	 * @param y
	 *            Y-coordinate.
	 * @param slice
	 *            Slice number.
	 * @param tac
	 *            Time-activity curve.
	 */
	public Voxel(int x, int y, int slice, double[] tac) {
		this.x = x;
		this.y = y;
		this.slice = slice;
		this.tac = tac;

	}

	public void setContrastRaw(int nFrames, int firstFrame) {
	}

	public void setContrastEstim(double aif[][]) {
	}

	public void setContrastFitted(fitter f) {
	}

	public void setParams() {
	}

	/*public int compareTo(Voxel o) {
		Voxel v = o;
		if (slice == v.slice && x == v.x && y == v.y)
			return 1;
		return 0;
	}*/

}
