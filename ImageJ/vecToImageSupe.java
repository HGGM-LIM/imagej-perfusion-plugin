
	import java.awt.event.KeyEvent;
import java.util.Vector;

	import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class vecToImageSupe {
	// width , height 


	public vecToImageSupe(ImagePlus ip, Vector<Voxel> voxels,Vector <Voxel> superp, int width,int height, int nSlices) {
			int[] dim = ip.getDimensions();
			ImagePlus res = IJ.createImage(ip.getTitle(), "16-bit", dim[0], dim[1],
					dim[3]);
			res.setDimensions(1, dim[3], 1);
			res.setDisplayRange(0, 400);
			ImageStack target = res.getStack();
			for (int i = 0; i < voxels.size(); i++) {
				target.setVoxel(voxels.get(i).x, voxels.get(i).y,
						voxels.get(i).slice - 1, voxels.get(i).tac[1]);
				for (int j=0; j < superp.size(); j++) {
					if (superp.get(j).slice == voxels.get(i).slice && superp.get(j).x == voxels.get(i).x && superp.get(j).y == voxels.get(i).y ){
						
						
					}
				}
			}
			
			res.show();
			IJ.run("In [+]");
			IJ.run("In [+]");
			IJ.run("In [+]");

		}

	}


