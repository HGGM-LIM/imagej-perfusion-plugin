import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class vecToStack extends ImagePlus {

	public vecToStack(ImagePlus ip, List<VoxelT2> voxels, String param) {
		double parameter = 0;
		int[] dim = ip.getDimensions();
		ImagePlus res = IJ.createImage(ip.getTitle(), "16-bit", dim[0], dim[1],
				dim[3]);
		res.setDimensions(1, dim[3], 1);
		res.setDisplayRange(0, 800);

		if (param == "CBV")
			res.setTitle("CBV");
		else if (param == "MTT")
			res.setTitle("MTT");

		ImageStack target = res.getStack();
		for (VoxelT2 v : voxels) {
			if (param == "CBV") 
				parameter = v.getCBV();
			 else 
				parameter = v.getMTT();

			target.setVoxel(v.x, v.y, v.slice - 1, 100 * parameter);

		}
		res.show();
		IJ.run("In [+]");
		IJ.run("In [+]");
		IJ.run("In [+]");

	}

}
