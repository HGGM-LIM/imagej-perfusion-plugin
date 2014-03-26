package perfusion;


import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;

import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Mac�as Gordaliza</a>
 * 
 * <p>
 * JPerfusionTool ImageJ Plugin.
 * </p>
 * 
 * <p>
 * The plugin calculates the CBF,CBV and MTT for MRI perfusion studies
 * </p>
 *
 */
public class JPerfusionTool_ implements PlugInFilter, ActionListener {
	
	ImagePlus hyStack;
	ImagePlusHyp myHypStk;
	List<VoxelT2> nonAllVoxels=new ArrayList<VoxelT2>(), notFit = new ArrayList<VoxelT2>();
	MainFrame mf;
	String voxelModel = "T2";
	EventUtils eu;

	public void run(ImageProcessor arg0) {
		IJ.showStatus("Start");
		mf = new MainFrame();
		mf.setVisible(true);
		mf.startButton.addActionListener(this);
		mf.addWindowListener(new WindowListener() {

			public void windowClosed(WindowEvent arg0) {
				eu.turnOff();
			}
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
			
		});
	}

	private void doIt() {
		
		myHypStk = new ImagePlusHyp(hyStack);

		/******* Masking *****************/

		double max = 0;
	
		IJ.showStatus("Adding Voxels...");
		Iterator<Voxel> voxIterator = getVoxelModel(); 

		boolean fBool = mf.comboFitting.getSelectedItem().toString() == "NoFitter";
		while (voxIterator.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator.next();
			if (v != null && (fBool || v.isFittable())) {
				nonAllVoxels.add(v);
				if (StatUtils.max(v.contrastRaw) > max) 
					max = StatUtils.max(v.contrastRaw);
			} else if (v != null)
				notFit.add(v);
		}
		

		EventUtils.showPointsOverlays(notFit);
		IJ.showStatus("All meaninful voxels added");

		// Show Image after mask
		vecToStack.paintParametricMap(myHypStk, nonAllVoxels, "Nada");
		
		

		//****************** FITTING ***********************

		fitter f = getFitter(mf.comboFitting.getSelectedItem());
		for (VoxelT2 v : nonAllVoxels) {
			v.setContrastFitted(f);
			if (v.getContrastFitted() != null) 
				v.setParams();
				v.AIFValidation(max);
		}
		// /////////////////////Enables the Dynamic Pixel Inspector/////////////////////////////////////////
		eu = new EventUtils(hyStack, nonAllVoxels, mf.showCont, new Plot("AIF", "Time", "Contrast"));
		eu.turnOn();
		////////////////AIF Calculation/////////////////////////

		AIF aifO = new AIF(nonAllVoxels);

		mf.AIFVoxels.addItemListener(aifO);
		
		double[] AIF = null ;
		aifO.setAIFfit(f);
		// //////////////////////
		if (aifO.getAIFfit() != null)
			AIF = aifO.getAIFfit();
		JOptionPane jop = new JOptionPane("               Is the AIF valid?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_OPTION, null);

		JDialog dialog = jop.createDialog("AIF Validation");
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);

		aifO.paint(hyStack);
		int b = 1;
		do {
			aifO.paintChart();
			dialog.setVisible(true);
			b = (Integer) jop.getValue();
			if (b == 1) {
				aifO.manualCalc(nonAllVoxels);
				aifO.setAIFfit(f);
				AIF = aifO.getAIFfit();
			}

		} while (b == 1);

		/////////////////////////AIF Calculation End/////////////////////

		double aifInt = MathUtils.interBad(AIF);
		for (VoxelT2 v : nonAllVoxels) {
			if (StatUtils.max(v.contrastFitted) < 2 * StatUtils.max(v.contrastRaw))
				v.setCBV(aifInt);
		}
		
		// Show CBV Image
		vecToStack.paintParametricMap(myHypStk, nonAllVoxels, "CBV");

		//********** Contrast without AIF influence ***********

		// ///pseudoinverse AIF///////////
		//double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon( MathUtils.lowTriangular(AIF));
		for (VoxelT2 v : nonAllVoxels) {
			v.setContrastEstim(matrixPAIF);
			double aMax = StatUtils.max(v.contrastEstim) > FastMath
					.abs(StatUtils.min(v.contrastEstim)) ? 1 : -1;

			if (aMax > 0 /*aMax < 1.2* StatUtils.max(v.contrastRaw)*/ ) {
				v.setMTT();
				v.setCBF();
			}
		}
		
		
		// Show MTT Image
		vecToStack.paintParametricMap(myHypStk, nonAllVoxels, "MTT");
		// Show CBF Image
		vecToStack.paintParametricMap(myHypStk, nonAllVoxels, "CBF");
		RoiManager.getInstance().setVisible(true);
		

	}
	
	public int setup(String arg0, ImagePlus arg1) {
	    if (arg1 == null) {
	        IJ.error("Need an open image");
	        return DONE;
	    }
	    
		if (!arg1.isHyperStack()) {
			if (arg1.getNChannels() == 1 && arg1.getNSlices() == 1
					&& arg1.getNFrames() > 1) {
				hyStack = arg1;
				return DOES_ALL + STACK_REQUIRED;
			}
			IJ.error("This plugin only works on HyperStacks.");
			return DONE;
		} else {
			hyStack = arg1;
			return DOES_ALL + STACK_REQUIRED;
		}
	}

	private fitter getFitter(Object object) {
		if (object == "Auto")
			return new GammaFitterACM();
		else if (object == "NoFitter")
			return new NoFitter();
		else if (object == "GammaFitterSVD")
			return new GammaFitterSVD();
		else if (object == "GammaFitterACM")
			return new GammaFitterACM();
		return null;
	}
	
	private Iterator<Voxel> getVoxelModel() {
		return myHypStk.getIterator(voxelModel,mf.ThrField.getText());
	}


	public void actionPerformed(ActionEvent e) {
		doIt();
	}

}
