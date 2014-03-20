import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías Gordaliza</a>
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
	List<VoxelT2> nonAllVoxels, notFit;

	MainFrame mf;

	public void run(ImageProcessor arg0) {
		IJ.showStatus("Start");
		mf = new MainFrame();
		mf.setVisible(true);
		mf.startButton.addActionListener(this);
	}

	
	private void doIt() {
		ImagePlusHyp myHypStk = new ImagePlusHyp(hyStack);

		/******* Masking *****************/

		int[] thresholds = new int[myHypStk.getNSlices()];
		double max = 0;

		int a = hyStack.getNSlices() / 2;

		int thr = (int) (myHypStk.getThreshold(a == 0 ? 1 : a) / Double
				.valueOf(mf.ThrField.getText()));
		for (int i = 1; i <= myHypStk.getNSlices(); i++) 
			thresholds[i - 1] = thr;
		

		IJ.showStatus("Addig Voxels...");
		voxIterator voxIterator2 = (voxIterator) myHypStk.iterator();
		nonAllVoxels = new ArrayList<VoxelT2>();
		notFit = new ArrayList<VoxelT2>();

		boolean fBool = mf.comboFitting.getSelectedItem().toString() == "NoFitter";
		while (voxIterator2.hasNext()) {
			VoxelT2 v = (VoxelT2) voxIterator2.next(thresholds);
			if (mf.sFit.isSelected() == true && v != null && v.te == -1
					&& v.notFalling((int) (hyStack.getNFrames() * 0.25 + 1)))
				v.te = v.contrastRaw.length - 1;
			if (v != null && (fBool || (v.t0 > 0 && v.te > 0))) {
				nonAllVoxels.add(v);

				if (StatUtils.max(v.contrastRaw) > max) {
					max = StatUtils.max(v.contrastRaw);
				}

			} else if (v != null)
				notFit.add(v);

		}

		EventUtils.showPointsOverlays(notFit);
		IJ.showStatus("All meaninful voxels added");

		new vecToStack(myHypStk, nonAllVoxels, "Nada");

		/******************* FITTING ************************/

		fitter f = getFitter(mf.comboFitting.getSelectedItem());

		for (VoxelT2 v : nonAllVoxels) {
			v.setContrastFitted(f);
			if (v.getContrastFitted() != null) 
				v.setParams();
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
		if (aifO.getAIFfit() != null)
			AIF = aifO.getAIFfit();
		JOptionPane jop = new JOptionPane("               Is the AIF valid?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_OPTION, null);

		jop.setEnabled(true);
		JDialog dialog = jop.createDialog("AIF Validation");

		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);

		aifO.paint(hyStack);
		int b = 1;
		while (b == 1) {
			aifO.paintChart();
			dialog.setVisible(true);
			b = (Integer) jop.getValue();
			if (b == 1) {
				aifO.manualCalc(nonAllVoxels);
				aifO.setAIFfit(f);
				AIF = aifO.getAIFfit();
			}

		}

		// //////////////////////////////////////

		double maxCBV = 0;
		double aifInt = MathUtils.interBad(AIF);
		for (VoxelT2 v : nonAllVoxels) {
			if (StatUtils.max(v.contrastFitted) < 2 * StatUtils
					.max(v.contrastRaw))
				v.setCBV(aifInt);
			if (maxCBV < v.getCBV()) 
				maxCBV = v.getCBV();
		}

		new vecToStack(myHypStk, nonAllVoxels, "CBV");

		new EventUtils(IJ.getImage(), nonAllVoxels, mf.showCont, ch).turnOn();

		/*********** Contrast without AIF influence ************/

		// ///pseudoinverse AIF///////////
		double[][] matrixAIF = MathUtils.lowTriangular(AIF);
		double[][] matrixPAIF = MathUtils.pInvMon(matrixAIF);
		double minMTT = 100, maxMTT = 0;

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

			if (v.getMTT() > maxMTT) 
				maxMTT = v.getMTT();

			if (v.getMTT() < minMTT) 
				minMTT = v.getMTT();
		}

		new vecToStack(myHypStk, nonAllVoxels, "MTT");
		new vecToStack(myHypStk, nonAllVoxels, "CBF");
		RoiManager.getInstance().setVisible(true);

	}

	public int setup(String arg0, ImagePlus arg1) {
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

	public void actionPerformed(ActionEvent e) {
		doIt();
	}

}
