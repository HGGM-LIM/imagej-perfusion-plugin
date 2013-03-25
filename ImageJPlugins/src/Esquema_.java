import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import org.apache.commons.math3.stat.StatUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Esquema_ implements PlugInFilter {
	String path = IJ
			.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script2.txt");
	ImagePlus hyStack;
	private JPanel main_panel;

	@Override
	public void run(ImageProcessor arg0) {
		MainFrame mf = new MainFrame();
		
		ImagePlusHyp myHypStk = new ImagePlusHyp(hyStack);
	   double mean= hyStack.getStatistics().mean;
		mf.setVisible(true);
		while(mf.startPressed==false);
	

	  /*******Masking*****************/
		int[] thresholds = new int[myHypStk.getNSlices()];
		double max=0,maxAux=0;
		VoxelT2 vMax=null;
		int thr = myHypStk.getThreshold(15);
		for (int i = 1; i <= myHypStk.getNSlices();i++){
			thresholds[i-1]=(int) (myHypStk.getThreshold(i));
			thresholds[i-1] = thr;
		}
		
		
	   
	    
		voxIterator voxIterator2 = (voxIterator) myHypStk.iterator();
		List<VoxelT2> nonAllVoxels = new ArrayList<VoxelT2>();
		
		while (voxIterator2.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator2.next(thresholds);
			//VoxelT2 v = (VoxelT2) voxIterator2.next(1.5);
			// TODO COGER SOLO LOS NO ENMASCARDADOS
			if ( v != null && v.t0 > 0 && v.te > 0  ){
				nonAllVoxels.add(v);
			
			maxAux=StatUtils.max(v.contrastRaw);
			if(maxAux > max) {
				max=maxAux;
				vMax=v;
			}
			//System.out.println(nonAllVoxels.size());
			}
	   
		}
		new vecToStack(myHypStk, nonAllVoxels,"Nada");
		


        /******************* FITTING**************************/
		// TODO Cast different fittings.
		
		fitter f = getFitter(mf.comboFitting.getSelectedItem());
		
		for (VoxelT2 v : nonAllVoxels) {
			if(v.x>55 && v.y >= 95 && v.slice>=20){
		    	 System.out.println();
		     }
				v.setContrastFitted(f);
				if(v.getContrastFitted() != null)
				v.setParams();
			}
	////////////////////////////////////////////////////////////////
		
		/* Contrast and important params */
		for (int i = 0; i < nonAllVoxels.size(); ) {
			 if(nonAllVoxels.get(i).x>=50 && nonAllVoxels.get(i).y >= 95 && nonAllVoxels.get(i).slice>=20){
		    	 System.out.println();
		     }
			 boolean a= nonAllVoxels.get(i).isNoisy(0.15);
			if (Double.compare(((VoxelT2) nonAllVoxels.get(i)).getFWHM(), Double.NaN) == 0 || Double.compare(nonAllVoxels.get(i).getFWHM(), 0) == 0 || a )
					nonAllVoxels.remove(i);	
				else 
					i++;	
		}
		
		new vecToStack(myHypStk, nonAllVoxels,"Nada");
		
	
	
		AIF aifO = new AIF(nonAllVoxels);
		
		aifO.paint(hyStack, aifO.getProbAIFs());
		double[] AIFC = aifO.getAIF();
		double[] AIF = MathAIF.getAIF(nonAllVoxels,false);
		//double[] AIF ={0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.792082605370844, 2.1589069632329827, 1.4782221304652146, 0.9897604635361539, 0.5724825986621263, 0.3672062050786013, 0.250498770078183, 0.14776454693893532, 0.09846422087299689, 0.05886883965214313, 0.034980226071307914, 0.024270195773319393, 0.014836931830294, 0.008513499411197114, 0.004825699791248541, 0.002753751975293374, 0.0015129451236820606, 8.773150817592962E-4, 6.388217837555207E-4, 3.6820106745557007E-4, 2.121299039032993E-4, 1.2217445088836366E-4, 7.035030093500616E-5, 4.050133903165233E-5, 2.331452746517061E-5, 1.3420078471215085E-5, 7.724470595084313E-6, 4.446096065090004E-6, 2.5591413250601873E-6};
		double aifInt = StatUtils.sum(AIF);
		for (int i = 0; i < nonAllVoxels.size(); i++) 
			 nonAllVoxels.get(i).setCBV(aifInt);
		
		
		new vecToStack(myHypStk, nonAllVoxels,"CBV");
		
	
		

		/***********Contrast without AIF influence************/
		
		/////pseudoinverse AIF///////////
		double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon(matrixAIF);
		
		
		for (VoxelT2 v : nonAllVoxels){
				if ( v.getContrastFitted() != null) {
					  if(v.x>55 && v.y >= 95 && v.slice>=20){
					    	 System.out.println();
					     }
					
				 v.setContrastEstim(matrixPAIF);
			     v.setMMT();      
			   
			}		
		}
		
			
		new vecToStack(myHypStk, nonAllVoxels,"MTT");
		System.out.println();
		hyStack.setActivated();
			

	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// IJ.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script.txt");
		hyStack = arg1;
		return DOES_ALL + STACK_REQUIRED;
	}
	
	private fitter getFitter(Object object) {
		if (object == "Auto")
			return new GammaFitter();
		else if(object == "NoFitter")
			return new NoFitter();
		else if(object == "GammaFitter")
			return new GammaFitter();
		return null;
	}

}
