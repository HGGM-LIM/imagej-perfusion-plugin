import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.StatUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Esquema_ implements PlugInFilter {
	String path = IJ
			.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script.txt");
	ImagePlus hyStack;

	@Override
	public void run(ImageProcessor arg0) {
		
		
		ImagePlusHyp myHypStk = new ImagePlusHyp(hyStack);

	  /*******Masking*****************/
		int[] thresholds = new int[myHypStk.getNSlices()];
		
		for (int i = 1; i <= myHypStk.getNSlices();i++)
			thresholds[i-1]=(int) (0.025*myHypStk.getThreshold(i));
	   
	    
		voxIterator voxIterator2 = (voxIterator) myHypStk.iterator();
		List<VoxelT2> nonAllVoxels = new ArrayList<VoxelT2>();
		
		while (voxIterator2.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator2.next(thresholds);
			// TODO COGER SOLO LOS NO ENMASCARDADOS
			if ( v != null && v.t0 > 0 && v.te > 0)
				nonAllVoxels.add(v);
			//System.out.println(nonAllVoxels.size());
	   
		}


        /******************* FITTING**************************/
		// TODO Cast diferent fittings.
		fitter f = new GammaFitter();
		//fitter f = new NoFitter();
		
		for (VoxelT2 v : nonAllVoxels) {
				v.setContrastFitted(f);
				if(v.getContrastFitted() != null)
				v.setParams();
			}
	////////////////////////////////////////////////////////////////
		
		/* Contrast and important params */
		for (int i = 0; i < nonAllVoxels.size(); ) {
				
				if (Double.compare(((VoxelT2) nonAllVoxels.get(i)).getFWHM(), Double.NaN) == 0 || Double.compare(((VoxelT2) nonAllVoxels.get(i)).getFWHM(), 0) == 0)
					nonAllVoxels.remove(i);	
				else 
					i++;	
		}
		
		
		double[] AIF = MathAIF.getAIF(nonAllVoxels);
		double aifInt = StatUtils.sum(AIF);
		for (int i = 0; i < nonAllVoxels.size(); i++) 
			 nonAllVoxels.get(i).setCBV(aifInt);
		
		
		new vecToStack(myHypStk, nonAllVoxels,"CBV");
		
	
		

		/***********Contrast without AIF influence************/
		
		/////pseudoinverse AIF///////////
		double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon(matrixAIF);
		
	
		for (VoxelT2 v : nonAllVoxels){
			//if(nonAllVoxels.get(i).x>58 && nonAllVoxels.get(i).y >= 110 && nonAllVoxels.get(i).slice>=20)

			if ( v.getContrastFitted() != null) {
				 v.setContrastEstim(matrixPAIF);
			     v.setMMT();      
			}		
		}
		
			
		new vecToStack(myHypStk, nonAllVoxels,"MTT");
		System.out.println();
			

	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// IJ.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script.txt");
		hyStack = arg1;
		return DOES_ALL + STACK_REQUIRED;
	}

}
