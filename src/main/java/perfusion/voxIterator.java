package perfusion;

/**
 * Allows different thresholds per slice
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Mac�as
 *         Gordaliza</a>
 * 
 */

// purpose intended is not used at last. Nice Grammar MothaFuckah
public class voxIterator extends ImagePlusHypIterator {
	int thr;
	double forceFit;

	public voxIterator(ImagePlusHyp ip) {
		super(ip);

	}

	public voxIterator(ImagePlusHyp ip, String... s) {
		this(ip);
		int a = ip.getNSlices() / 2;
		thr = (int) (ip.getThreshold(a == 0 ? 1 : a) / Double.parseDouble(s[1]));
		forceFit = Double.parseDouble(s[2]);
		if (forceFit > 1)
			forceFit = 1;
		else if (forceFit < 0)
			forceFit = 0;
	}

	public Voxel next() {
		return nextT2(thr);
	}

	public Voxel next(int threshold) {
		if (ip.getFirstPixel(x, y, slice) > threshold)
			return next();
		else {
			_updatePointers();
			return null;
		}
	}

	public Voxel next(int[] thresholds) {
		return nextT2(thresholds[slice - 1]);
	}

	public Voxel nextT2(int threshold) {
		if (ip.getFirstPixel(x, y, slice) > threshold) {
			// forceFit = 0.25 default
			VoxelT2 v2 = new VoxelT2(super.next());
			if (/* mf.sFit.isSelected() == true && */v2 != null && v2.te == -1
					&& v2.notFalling((int) (ip.getNFrames() * forceFit + 1)))
				v2.te = v2.contrastRaw.length - 1;

			return v2;
		} else {
			_updatePointers();
			return null;
		}
	}

	

}
