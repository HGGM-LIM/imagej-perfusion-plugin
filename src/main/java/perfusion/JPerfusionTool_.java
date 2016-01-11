package perfusion;


import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;






import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
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


	ImageIcon continueIcon =  new ImageIcon(JPerfusionTool_.class.getResource("/continue-icon.png"));
	ImageIcon biigIcon =  new ImageIcon(JPerfusionTool_.class.getResource("/BIIG.png"));
	ImageIcon questionIcon =  new ImageIcon(JPerfusionTool_.class.getResource("/Question_mark.png"));
	AIF aifO;
	/** Options for AIF Menu
	 * Each new option needs a new AIFCalculator which must be indicated within calcAIF(n) method*/
	public final Object[] AIFoptions = {"Semi-Manual Calculation","AIF from a text file","Cancel"};




	public void run(ImageProcessor arg0) {
		IJ.showStatus("Start");
		System.out.println("Paso Run");


		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//mf = new MainFrame();
				mf.setIconImage(JPerfusionTool_.this.biigIcon.getImage());
				mf.setVisible(true);

		//mf.setVisible(true);
		mf.startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doIt();
			}
		});
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

		aifO = new AIF(nonAllVoxels);

		mf.AIFVoxels.addItemListener(aifO);

		double[] AIF = null ;
		aifO.setAIFfit(f);
		// //////////////////////
		if (aifO.getAIFfit() != null)
			AIF = aifO.getAIFfit();

		JOptionPane jop = new JOptionPane("               Is the AIF valid?",
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_OPTION, null);
		jop.setIcon(this.questionIcon);

		JDialog dialog = jop.createDialog("AIF Validation");
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setIconImage(biigIcon.getImage());

		aifO.paint(hyStack);


		int b = 1;
		do {
			aifO.paintChart();
			dialog.setVisible(true);
			b = (Integer) jop.getValue();
			if (b == 1) {
				int n = JOptionPane.showOptionDialog(jop,
					    "Choose your way: ",
					    "AIF Calculation",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    this.continueIcon,
					    AIFoptions,
					    AIFoptions[AIFoptions.length - 1]);
				calcAIF(n);
				aifO.setAIFfit(f);
				AIF = aifO.getAIFfit();
			}

		} while (b == 1);

		if(mf.saveAIF.isSelected())
			aifO.saveAIF(hyStack.getOriginalFileInfo().directory+hyStack.getShortTitle()+new SimpleDateFormat("_yyyyMMdd-HHmmss").format(new Date())+".csv");


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

		double[][] matrixPAIF = MathUtils.pInvMon( MathUtils.lowTriangular(AIF));
		for (VoxelT2 v : nonAllVoxels){
			v.setContrastEstim(matrixPAIF);
			double aMax = StatUtils.max(v.contrastEstim) > FastMath.abs(StatUtils.min(v.contrastEstim)) ? 1 : -1;

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



private void AIFMethodSelector(/*JDialog dialog,JOptionPane jop,fitter f, AIF aif*/) {
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JButton button = new JButton("b");
	panel.add(button);
	frame.add(panel);
	frame.pack();
	frame.setVisible(true);
	SwingWorker sw = new SwingWorker<Void,Integer>() {

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			for (int i = 0; i  < 1000000; i++){
				//setProgress(i);
				//Thread.sleep(100);
				System.out.println("Task "+i);
			}

			return null;
		}

		public void done() {
			System.out.println("Done");

		}

	};
	System.out.println("Execute");
	sw.execute();
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
			//String params = Macro.getOptions();
			MainFrame.setParams(Macro.getOptions());
			mf = new MainFrame();
			hyStack = arg1;
			return DOES_ALL + STACK_REQUIRED;
		}
	}

	/*Neccesary to include here the correspondent number per each AIFOptions*/
	private void calcAIF(int n) {
		switch(n) {
		case 0:
			aifO.doAIFCalculation(new AIFManualSelection(aifO,nonAllVoxels ));
			break;
		case 1:
			JFileChooser jfc = new JFileChooser();
			jfc.showOpenDialog(new JFrame());
			aifO.doAIFCalculation(new AIFFromTextFile(jfc.getSelectedFile()));
			break;
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
		return myHypStk.getIterator(voxelModel,mf.ThrField.getText(),mf.forceFake.getText());
	}


	public void actionPerformed(ActionEvent e) {
		doIt();
	}

	/*public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = JPerfusionTool_.class;
		URL main = clazz.getResource("JPerfusionTool_.class");
		if (!"file".equalsIgnoreCase(main.getProtocol()))
		  throw new IllegalStateException("GetDirectory class is not stored in a file.");
		File path = new File(main.getPath()).getParentFile().getParentFile();
		String pluginsDir = path.getAbsolutePath();
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("/home/pmacias/Projects/JPerfusion/anonymous.tif");
		image.show();

		// run the plugin
		//IJ.run(image, "JPerfusionTool ", "ShowContrast=False,ShowAIFVoxels=True,SaveFileAIF=False,ThrRel=0.75,ForceFit=1");
		//IJ.run(image, "JPerfusionTool ", "ShowContrast=False,ShowAIFVoxels=True,SaveFileAIF=True,ThrRel=1,ForceFit=0.25"); //default
		//IJ.runPlugIn(clazz.getName(), "False,True,False,0.75,1");

	}*/

}
