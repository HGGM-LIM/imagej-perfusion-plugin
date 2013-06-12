
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import ij.IJ;
import ij.IJEventListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class JPerfusionTool_ implements PlugInFilter {
	//String path = IJ
		//	.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script2.txt");
	ImagePlus hyStack;
	List<VoxelT2> nonAllVoxels;
	private JPanel main_panel;
	

	@Override
	public void run(ImageProcessor arg0) {
		
		MainFrame mf = new MainFrame();
		ImagePlusHyp myHypStk = new ImagePlusHyp(hyStack);
	  // double mean= hyStack.getStatistics().mean;
		mf.setVisible(true);
		while(mf.startPressed==false);
	

	  /*******Masking*****************/
		int[] thresholds = new int[myHypStk.getNSlices()];
		double max=0,maxAux=0;
		VoxelT2 vMax=null;
		int a=hyStack.getNSlices()/2;
		
		int thr = (int) (myHypStk.getThreshold( a == 0 ? 1:a ) / 1.1);
		for (int i = 1; i <= myHypStk.getNSlices();i++){
			//thresholds[i-1]=(int) (myHypStk.getThreshold(i));
			thresholds[i-1] = thr;
		}
		
		
	   
	    
		voxIterator voxIterator2 = (voxIterator) myHypStk.iterator();
		 nonAllVoxels = new ArrayList<VoxelT2>();
		
		while (voxIterator2.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator2.next(thresholds);
			//VoxelT2 v = (VoxelT2) voxIterator2.next(1.5);
			// TODO COGER SOLO LOS NO ENMASCARDADOS
			//TODO Ojo te=-1
			//if(v != null&& v.x>= 61 && v.y >= 46 && v.slice == 23)
				//System.out.println();
			if(mf.sFit.isSelected() == true && v != null&& v.te == -1 && v.notFalling(5)) 
				 v.te = v.contrastRaw.length - 1 ;
			if ( v != null && v.t0 > 0 && v.te > 0   ){
				nonAllVoxels.add(v);
			
			
				
			if(StatUtils.max(v.contrastRaw) > max) {
				max=StatUtils.max(v.contrastRaw);
				
			}
			//System.out.println(nonAllVoxels.size());
			}
	   
		}
		
		
		new vecToStack(myHypStk, nonAllVoxels,30000,"Nada");
		


        /******************* FITTING**************************/
		// TODO Cast different fittings.
		
		fitter f = getFitter(mf.comboFitting.getSelectedItem());
	
		VoxelT2 voxelaco= nonAllVoxels.get(0);
		for (VoxelT2 v : nonAllVoxels) {
			//if(!v.isNoisy(0.15))
				v.setContrastFitted(f);
			//else
				//v.contrastFitted = null;
				if(v.getContrastFitted() != null ){
				v.setParams();
				}
			}
	////////////////////////////////////////////////////////////////
		
		
		
		new vecToStack(myHypStk, nonAllVoxels,max,"Nada");
		new EventUtils(hyStack,nonAllVoxels,mf.showCont).turnOn();
	
		
		////////////////////////////////////////
	
	
		AIF aifO = new AIF(nonAllVoxels,max);
		
		mf.AIFVoxels.addItemListener(aifO);
		
		double[] AIF = aifO.getAIF();
		
		//double[] AIF ={0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6926841336298653, 2.2324440658260905, 3.1371797309479312, 2.5298599426892068, 1.5935887778258893, 0.851058125557948, 0.4154007373529816, 0.17129676435706645, 0.06168409440693416, 0.02511351289655198, 0.009495270050608977, 0.003734759908372432, 0.0012936262647201466, 4.387568096030495E-4, 1.4637707370246457E-4, 4.821302407785589E-5, 1.5726165552045442E-5, 5.092678585377635E-6, 1.640770174170811E-6, 6.122058881290931E-7, 2.1613656285111652E-7, 6.521714180026211E-8, 1.9514803704573546E-8, 5.818380752154218E-9, 1.7150116169985371E-9, 5.02439303679748E-10, 1.463778824027736E-10, 4.242654570784719E-11, 1.223886376045624E-11, 3.51508715256437E-12};
        double maxCBV =0;
		double aifInt = StatUtils.sum(AIF);
		for (VoxelT2 v:nonAllVoxels) {
			 v.setCBV(aifInt);
			 if (maxCBV < v.getCBV()) maxCBV = v.getCBV();
		}
		
		
		new vecToStack(myHypStk, nonAllVoxels,maxCBV,"CBV");
		
	
		

		/***********Contrast without AIF influence************/
		
		/////pseudoinverse AIF///////////
		double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon(matrixAIF);
		double minMTT = 100,maxMTT = 0;
		VoxelT2 sliM=null, slim=null; ;
		
		for (VoxelT2 v : nonAllVoxels){
				if ( v.getContrastFitted() != null ) {
					  if(v.x>=52 && v.y >= 61 && v.slice>=23 ){
					    	 System.out.println();
					     }
				if(v.AIFValid == true)
					 System.out.println();
				
					
				 v.setContrastEstim(matrixPAIF);
				 //TODO AL pponer a 39 todos;para hacerlo bien abria que ajustar solo los que suben y no bajan a buen nivel
				 if(StatUtils.max(v.contrastFitted) < 1.2* StatUtils.max(v.contrastRaw) )
			     v.setMMT(); 
			     if (v.getMTT() < 0)
			    	 System.out.println();
			     if(v.getMTT() > maxMTT) { maxMTT = v.getMTT();sliM = v;}
			     if(v.getMTT() < minMTT) {minMTT = v.getMTT(); slim = v;}
			   
			}		
		}
		
			
		new vecToStack(myHypStk, nonAllVoxels,maxMTT,"MTT");
		System.out.println();
		hyStack.setActivated();
			

	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		hyStack = arg1;
		return DOES_ALL + STACK_REQUIRED;
	}
	
	private fitter getFitter(Object object) {
		if (object == "Auto")
			return new autoGamma();
		else if(object == "NoFitter")
			return new NoFitter();
		else if(object == "GammaFitter")
			return new GammaFitter();
		else if(object == "autoGamma")
			return new autoGamma();
		return null;
	}
	

}