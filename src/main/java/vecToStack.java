
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

/**
 * Show an Image from a voxels list
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Mac�as Gordaliza</a>
 *
 */
public class vecToStack {

	/**
	 * @param ip
	 * @param voxels
	 * @param param Parameter to show
	 */
	public static void paintParametricMap(ImagePlus ip, List<VoxelT2> voxels,String param) {
		double parameter = 0;
		int[] dim = ip.getDimensions();

		ImagePlus res = IJ.createImage(ip.getTitle(),"32-bit", dim[0], dim[1],
				dim[3]);
		res.setDimensions(1, dim[3], 1);

		if (param == "CBV")
			res.setTitle("CBV");
		else if (param == "MTT")
			res.setTitle("MTT");
		else if (param == "CBF")
			res.setTitle("CBF");
		else if (param == "Nada")
			res.setTitle("Normal");

		
		ImageStack target = res.getStack();
		for (VoxelT2 v : voxels) {
			if (param == "CBV") 
				parameter = v.getCBV();
			 else if(param == "MTT")
				parameter = v.getMTT();
			 else if(param == "CBF")
				 parameter = v.getCBF();
			 else if(param == "Nada")
				 parameter = v.tac[0];

			target.setVoxel(v.x, v.y, v.slice - 1,  parameter);
		}
		 Calibration cal = ip.getCalibration();
	        double size_x = cal.pixelWidth;
	        double size_y = cal.pixelHeight;
	        double size_z = cal.pixelDepth;
	        String units = cal.getUnit();
	        String vscomm = "setVoxelSize(" + size_x + ", " + size_y + ", "
	                                      + size_z + ", \"" + units + "\")";
	        IJ.runMacro(vscomm);
		IJ.run(res, "Enhance Contrast", "saturated=0.35");
		res.show();
		IJ.run("In [+]");
		IJ.run("In [+]");
		IJ.run("In [+]");

	}

}
