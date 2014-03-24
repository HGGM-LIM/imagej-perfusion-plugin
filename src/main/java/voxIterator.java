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

	public voxIterator(ImagePlusHyp ip) {
		super(ip);

	}

	public voxIterator(ImagePlusHyp ip, String... s) {
		this(ip);
		int a = ip.getNSlices() / 2;
		this.thr = (int) (ip.getThreshold(a == 0 ? 1 : a) / Double
				.parseDouble(s[1]));
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
			
			
			VoxelT2 v2= new VoxelT2(super.next());
			if (/*mf.sFit.isSelected() == true &&*/ v2 != null && v2.te == -1
			&& v2.notFalling((int) (ip.getNFrames() * 0.25 + 1)))
				v2.te = v2.contrastRaw.length - 1;
			//return new VoxelT2(super.next());
			return v2;
		} else {
			_updatePointers();
			return null;
		}
	}
	
	

	/*
	 * public VoxelT2 next(double a) { //Voxel v = next(); return new
	 * VoxelT2(super.next()); }
	 */

}
