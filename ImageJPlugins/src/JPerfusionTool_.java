import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

public class JPerfusionTool_ implements PlugInFilter, ActionListener {
	//String path = IJ
		//	.runMacroFile("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\script2.txt");

	ImagePlus hyStack;
	List<VoxelT2> nonAllVoxels;
	// private JPanel main_panel;
	MainFrame mf;

	@Override
	public void run(ImageProcessor arg0) {
		IJ.showStatus("Start");
		// hyStack.setType(ImagePlus.GRAY32);
		mf = new MainFrame();
		// double mean= hyStack.getStatistics().mean;
		mf.setVisible(true);
		mf.startButton.addActionListener(this);
	}

	public void doIt() {

		ImagePlusHyp myHypStk = new ImagePlusHyp(hyStack);

		/******* Masking *****************/
		// IJ.showMessage("Aqui");
		int[] thresholds = new int[myHypStk.getNSlices()];
		double max = 0, maxAux = 0;
		VoxelT2 vMax = null;
		int a = hyStack.getNSlices() / 2;

		int thr = (int) (myHypStk.getThreshold(a == 0 ? 1 : a)  /*/ 1.1*/ );
		for (int i = 1; i <= myHypStk.getNSlices(); i++) {
			// thresholds[i-1]=(int) (myHypStk.getThreshold(i));
			thresholds[i - 1] = thr;
		}

		IJ.showStatus("Addig Voxels...");
		voxIterator voxIterator2 = (voxIterator) myHypStk.iterator();
		nonAllVoxels = new ArrayList<VoxelT2>();

		while (voxIterator2.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator2.next(thresholds);
			// VoxelT2 v = (VoxelT2) voxIterator2.next(1.5);
			// TODO COGER SOLO LOS NO ENMASCARDADOS
			// TODO Ojo te=-1
			// if(v != null&& v.x>= 61 && v.y >= 46 && v.slice == 23)
			// System.out.println();
			if (mf.sFit.isSelected() == true && v != null && v.te == -1
					&& v.notFalling(10))
				v.te = v.contrastRaw.length - 1;
			if (v != null && v.t0 > 0 && v.te > 0) {
				nonAllVoxels.add(v);

				if (StatUtils.max(v.contrastRaw) > max) {
					max = StatUtils.max(v.contrastRaw);

				}
				// System.out.println(nonAllVoxels.size());
			}

		}

		new vecToStack(myHypStk, nonAllVoxels, 30000, "Nada");

		/******************* FITTING **************************/
		// TODO Cast different fittings.

		fitter f = getFitter(mf.comboFitting.getSelectedItem());

		for (VoxelT2 v : nonAllVoxels) {
			// if(!v.isNoisy(0.15))
			v.setContrastFitted(f);
			// else
			// v.contrastFitted = null;
			if (v.getContrastFitted() != null) {
				v.setParams();
			}
		}
		// //////////////////////////////////////////////////////////////

		Plot ch = new Plot("AIF", "Time", "Contrast");
		new EventUtils(hyStack, nonAllVoxels, mf.showCont, ch).turnOn();

		// //////////////////////////////////////

		AIF aifO = new AIF(nonAllVoxels, max);

		mf.AIFVoxels.addItemListener(aifO);

		double[] AIF = aifO.getAIF();
		aifO.setAIFfit(f);
		// //////////////////////
		AIF = aifO.getAIFfit();
		JOptionPane jop = new JOptionPane("Are you happy enough with the AIF?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_OPTION, null);
		
		jop.setEnabled(true);
		JDialog dialog = jop.createDialog("AIF Validation");
		

		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		// dialog.setVisible(true);

		aifO.paint(hyStack);
		int b = 1;
		while (b == 1) {
			aifO.paintChart();
			dialog.setVisible(true);
			b = (int) jop.getValue();
			if (b == 1){
				aifO.manualCalc(nonAllVoxels);
			    aifO.setAIFfit(f);
			   AIF = aifO.getAIFfit();
			}

		}
		

		// //////////////////////////////////////
		VoxelT2 sM = null;
		double maxCBV = 0;
		double aifInt = MathUtils.interBad(AIF);
		for (VoxelT2 v : nonAllVoxels) {
			if (StatUtils.max(v.contrastFitted) < 2 * StatUtils
					.max(v.contrastRaw))
				v.setCBV(aifInt);
			if (maxCBV < v.getCBV()) {
				maxCBV = v.getCBV();
				sM = v;
			}

		}

		new vecToStack(myHypStk, nonAllVoxels, maxCBV, "CBV");
		new EventUtils(IJ.getImage(), nonAllVoxels, mf.showCont, ch).turnOn();

		/*********** Contrast without AIF influence ************/

		// ///pseudoinverse AIF///////////
		double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon(matrixAIF);
		double minMTT = 100, maxMTT = 0;
		VoxelT2 sliM = null, slim = null;
		;

		for (VoxelT2 v : nonAllVoxels) {
			if (v.AIFValid == true)
				System.out.println();
			v.setContrastEstim(matrixPAIF);

			double aMax = StatUtils.max(v.contrastEstim) > FastMath
					.abs(StatUtils.min(v.contrastEstim)) ? 1 : -1;
			if (aMax > 0/* aMax < 1.2* StatUtils.max(v.contrastRaw) */) {
				v.setMTT();
				v.setCBF();
			}

			if (v.getMTT() > maxMTT) {
				maxMTT = v.getMTT();
				sliM = v;
			}
			if (v.getMTT() < minMTT) {
				minMTT = v.getMTT();
				slim = v;
			}

		}

		new vecToStack(myHypStk, nonAllVoxels, maxMTT, "MTT");
		new vecToStack(myHypStk, nonAllVoxels, maxCBV, "CBF");
		RoiManager.getInstance().setVisible(true);
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		hyStack = arg1;
		return DOES_ALL + STACK_REQUIRED;
	}

	private fitter getFitter(Object object) {
		if (object == "Auto")
			return new autoGamma();
		else if (object == "NoFitter")
			return new NoFitter();
		else if (object == "GammaFitter")
			return new GammaFitter();
		else if (object == "autoGamma")
			return new autoGamma();
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		doIt();
	}

}
