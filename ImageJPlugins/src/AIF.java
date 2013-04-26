import ij.IJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.frame.RoiManager;

import java.util.ArrayList;
import java.util.List;


public class AIF {
	private List<VoxelT2> probAIFs;
	private List<VoxelT2> AIFValid;
	private double[] AIF;
	

	public AIF(List<VoxelT2> AllVoxels) {
		/* Contrast and important params */
		/*for (int i = 0; i < nonAllVoxels.size(); ) {
			 if(nonAllVoxels.get(i).x>=50 && nonAllVoxels.get(i).y >= 95 && nonAllVoxels.get(i).slice>=20){
		    	 System.out.println();
		     }
			 boolean a= nonAllVoxels.get(i).isNoisy(0.15);
			if (Double.compare(((VoxelT2) nonAllVoxels.get(i)).getFWHM(), Double.NaN) == 0 || Double.compare(nonAllVoxels.get(i).getFWHM(), 0) == 0 || nonAllVoxels.get(i).getMMC() > 17 )
					nonAllVoxels.remove(i);	
				else 
					i++;	
		}*/
		
		//TODO a�adir funcion MMC
			AIFValid = new ArrayList<VoxelT2>();
		for (VoxelT2 v : AllVoxels) {
			// TODO 0.75 por dependiente del max
			if(Double.compare(v.getFWHM(), Double.NaN) != 0 && Double.compare(v.getFWHM(), 0) > 0 && !v.isNoisy(0.125) && v.getMC() > 0.75 ) {
				AIFValid.add(v);
				//v.AIFValid = true;
			}
				
		}
		probAIFs = MathAIF.getAIFs(AIFValid);
		AIF = MathAIF.getAIF(probAIFs, true);
		
	}
	
	public AIF(double[] values) {
		probAIFs = null;
		AIF = values;
	}


	public double[] getAIF() {
		return AIF;
	}


	public void setAIF(double[] aIF) {
		AIF = aIF;
	}
	
	public List<VoxelT2> getProbAIFs() {
		return probAIFs;
	}
	
	public void paint(ImagePlus image,List<VoxelT2> voxels) {
		RoiManager manager =RoiManager.getInstance();
		for(VoxelT2 v : voxels) {
			PointRoi pr = new PointRoi(v.x , v.y);
			
			
			//RoiManager manager = RoiManager.getInstance();
			if (manager == null)
				manager = new RoiManager();
			IJ.runMacro("setSlice("+v.slice+")");
			image.setRoi(pr);
			manager.addRoi(image.getRoi());
			manager.runCommand("Associate", "true");
			
		}
		
	}

}
