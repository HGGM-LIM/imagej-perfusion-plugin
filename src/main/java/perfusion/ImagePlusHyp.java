package perfusion;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

/**
 * This class extends {@link ImagePlus} in order to add a handy {@link #getTAC}
 * method that allows to easily grab time-activity curves.
 * 
 * @author <a href="mailto:jmmateos@mce.hggm.es">José María Mateos</a>.
 * 
 */
public class ImagePlusHyp extends ImagePlus implements Iterable<Voxel> {

	/*
	 * dim[0] -> width (x) dim[1] -> height (y) dim[2] -> nChannels (1-based)
	 * dim[3] -> nSlices (1-based) dim[4] -> nFrames (1-based)
	 */
	private int[] dim;
	private ImageStack is;
	private Calibration cal;

	/**
	 * Creates a new ImagePlusHyp using a general ImagePlus.
	 * 
	 * @param ip
	 *            The ImagePlus used to construct this class.
	 */
	public ImagePlusHyp(ImagePlus ip) {

		super(ip.getTitle(), ip.getStack());
		this.is = ip.getStack();
		this.setProcessor(ip.getProcessor());
		this.setImage(ip);
		this.dim = ip.getDimensions();

		// Set the current calibration
		Calibration cal = ip.getCalibration();
		this.setCalibration(cal);
		this.cal = cal;

	}

	/**
	 * Gets the time-activity curve (dixel, after "dynamic pixel") for the given
	 * coordinates. Please note that this method returns the calibrated curve
	 * according to calibration data present in the image header, not the raw
	 * values. Please refer to ImageJ's {@link Calibration} object documentation
	 * for more information regarding this.
	 * 
	 * @param x
	 *            The x coordinate of the desired dixel.
	 * @param y
	 *            The y coordinate of the desired dixel.
	 * @param slice
	 *            Slice (1-based) of the desired dixel.
	 * @return A double array containing the values for the given voxel on each
	 *         frame, or null if the coordinates are not valid.
	 */
	public double[] getTAC(int x, int y, int slice) {

		// Dimension check
		if (x >= dim[0] || x < 0 || y >= dim[1] || y < 0 || slice > dim[3]
				|| slice < 1) {
			return null;
		}

		// Allocating space for the result
		double[] result = new double[dim[4]];

		// Set the desired slice and iterate through the frames
		for (int frame = 1; frame <= dim[4]; frame++) {
			int stack_number = this.getStackIndex(dim[2], slice, frame);
			// Use calibration to return true value
			result[frame - 1] = cal.getCValue(is.getVoxel(x, y,
					stack_number - 1));

		}

		return result;

	}

	/**
	 * Sometimes the whole TAC is not nedded
	 * 
	 * @param x
	 *            The x coordinate for the pixel sought
	 * @param y
	 *            The x coordinate for the pixel sought
	 * @param slice
	 * @return pixel intensity
	 */

	public double getFirstPixel(int x, int y, int slice) {
		if (x >= dim[0] || x < 0 || y >= dim[1] || y < 0 || slice > dim[3]
				|| slice < 1) {
			return Double.NaN;
		}
		int stack_number = this.getStackIndex(dim[2], slice, 1);
		return cal.getCValue(is.getVoxel(x, y, stack_number - 1));
	}

	/**
	 * 
	 * @param slice
	 *            The slice where we want to know the threshold for the whole
	 *            time sequence
	 * @return The threshold sought
	 */
	public int getThreshold(int slice) {
		return is.getProcessor(slice).getAutoThreshold();
	}

	/**
	 * @param data
	 *            The TAC to be tested
	 * @return true if the given TAC is noise with respect to this image. A TAC
	 *         is considered noise if the absolute value of its minimum value is
	 *         greater or equal than its maximum value. This is a very
	 *         simplistic approach, but works in most cases.
	 */
	public boolean isNoise(double[] data) {
		if (StrictMath.abs(StatUtils.min(data)) >= StatUtils.max(data))
			return true;
		return false;
	}
	


	public Iterator<Voxel> iterator() {
		return new ImagePlusHypIterator(this);
		// return new voxIterator(this);
	}

	public Iterator<Voxel> getIterator(String... s) {
		//TODO cambiar a tipo de voxel el sstring
		if (s[0] == "simple")
			return iterator();
		else if (s[0].compareToIgnoreCase("T2") == 0)
			return new voxIterator(this,s);

		return null;
	}

}
