
/**
 * Allows different thresholds per slice
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías Gordaliza</a>
 *
 */

//purpose intended is not used at last
public class voxIterator extends ImagePlusHypIterator {

	public voxIterator(ImagePlusHyp ip) {
		super(ip);
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
		return nextT2(thresholds[slice-1]);
	}
	
	public Voxel nextT2 (int threshold) {
		if (ip.getFirstPixel(x, y, slice) > threshold) {
			Voxel v = next();
    		return new VoxelT2(v.x,v.y,v.slice,v.tac);
		}
    	else {
    		_updatePointers();
    		return null;
    	}
	}
	
	public VoxelT2 next(double a) {
		Voxel v = next();
		return new VoxelT2(v.x,v.y,v.slice,v.tac);
	}
	

}
