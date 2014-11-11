package perfusion;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Dialog.ModalityType;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.math3.stat.StatUtils;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;

/**
 * AIF class contains the voxels used for Arterial Input Function calculation
 * and the fitted version. Also implements the methods for the AIF properties
 * visualitation
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías
 *         Gordaliza</a>
 * 
 */
public class AIF implements ItemListener, WindowListener {
	/*
	 * probAIFs are the voxels with a proper contrast to be AIF.
	 */
	protected List<VoxelT2> probAIFs;
	protected List<VoxelT2> AIFValid= new ArrayList<VoxelT2>();;
	private double[] AIF;
	private double[] AIFfit;
	protected JFrame jf;
	Plot AIFChart;
	PlotWindow AIFWindow;
	RoiManager manager = new RoiManager();
	JCheckBox jcb;
	boolean cB;
	//static Object lock = new Object();

	/**
	 * Class constructor
	 * 
	 * @param AllVoxels
	 *            The selected voxels for calculating the AIF
	 * @param max
	 *            The maximum value in the set of curves
	 */
	public AIF(List<VoxelT2> AllVoxels) {
		for (VoxelT2 v : AllVoxels)
			// Make it maximum dependent
			if (v.AIFValid)
				AIFValid.add(v);

		probAIFs = MathAIF.getAIFs(AIFValid);
		doAIFCalculation(new AIFAutomaticCalculation(this,AllVoxels));
	
		
		manager.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {
				jcb.setSelected(false);	
			}
			public void windowClosing(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
		}); 
	}

	/**
	 * Class constructor, permits to create an AIF directly from the values
	 * 
	 * @param values
	 *            new values for the AIF to be considered.
	 */

	public AIF(double[] values) {
		probAIFs = null;
		AIF = values;
	}

	/**
	 * 
	 * @return AIF values
	 */
	public double[] getAIF() {
		return AIF;
	}

	/**
	 * Change the AIF values using the parameter
	 * 
	 * @param AIF
	 *            new AIF values
	 */
	public void setAIF(double[] AIF) {
		this.AIF = AIF;
	}

	/**
	 * Get for the voxels whit AIF properties
	 * 
	 * @return probAIFs
	 */
	public List<VoxelT2> getProbAIFs() {
		return probAIFs;
	}


	/**
	 * Permits to fit the AIF by using a fitter extended from {@link fitter}
	 * 
	 * @param f
	 *            The kind of {@link fitter}
	 */
	public void setAIFfit(fitter f) {
		f.setup(AIF, MathUtils.minL(AIF), MathUtils.minR(AIF));
		boolean fitted = f.fitting();
		if (fitted == true)
			AIFfit = f.getFit();
		else {
			IJ.showMessage("It was impossible to fit the selected AIF \nClick OK and select again");
		}
	}

	/**
	 * Return the values AIF fitted curve v
	 * 
	 * @return AIFfit
	 */
	public double[] getAIFfit() {
		return AIFfit;
	}

	/**
	 * Draw the voxels used for the AIF calculation ( {@link #probAIFs} ) within
	 * the {@link ImagePlus} selected
	 * 
	 * @param image
	 *            The {@link ImagePlus}
	 */
	public void paint(ImagePlus image) {
		
		for (VoxelT2 v : probAIFs) {
			PointRoi pr = new PointRoi(v.x, v.y);
			pr.setPosition(1, v.slice, 1);

			 IJ.runMacro("setSlice(" + v.slice + ")");
			//pr.setName(v.x + "," + v.y + "," + v.slice + "-AIF");
			 image.setRoi(pr);
			manager.addRoi(pr/* image.getRoi() */);
			manager.runCommand("Associate", "true");

		}
		manager.setName("AIF ROIs");
		manager.runCommand("Show All");
		manager.setVisible(true);
	}

	/**
	 * Displays the AIF calculated and its fitted version
	 * 
	 */
	public void paintChart() {
		double[] x = new double[AIF.length];
		double contMax, contMin;
		for (int i = 0; i < x.length; i++)
			x[i] = i;
		AIFChart = new Plot("AIF", "Time", "Contrast", x, AIF);
		if (AIFfit != null) {
			contMax = StatUtils.max(AIF) > StatUtils.max(AIFfit) ? StatUtils
					.max(AIF) : StatUtils.max(AIFfit);
			contMin = StatUtils.min(AIF) < StatUtils.min(AIFfit) ? StatUtils
					.min(AIF) : StatUtils.min(AIFfit);
		} else {
			contMax = StatUtils.max(AIF);
			contMin = StatUtils.min(AIF);
		}
		AIFChart.setLimits(StatUtils.min(x), StatUtils.max(x), contMin, contMax);
		if (AIFfit != null) {
			AIFChart.addPoints(x, AIFfit, Plot.LINE);
			AIFChart.setColor(java.awt.Color.RED);
		}

		if (AIFWindow != null)
			AIFWindow.close();

		AIFWindow = AIFChart.show();
	}


	public void itemStateChanged(ItemEvent e) {
		jcb = (JCheckBox) e.getSource();
		if (jcb.isSelected()) {
			this.paint(IJ.getImage());
			manager.setVisible(true);

		} else if (manager != null) {
				
				manager.runCommand("Select All");
				manager.runCommand("Delete");
				manager.setVisible(false);
			}
	
		
	}

	
	/** Set {@link #AIF} from a user {@link AIFCalculator} **/
	public void doAIFCalculation(AIFCalculator aifCalc) {
		AIF = aifCalc.doAIFCalculation();
	}
	
	
	////////////////////Events////////////////////////////////////////////////////////
	public void windowClosed(WindowEvent e) {
		System.out.println("Closed");
		manager.removeAll();
		manager.close();
		manager = null;
	}
	

	private void voxelsAIFOverlay() {
		Overlay overlay = EventUtils.createOverlay(probAIFs);
		overlay.setFillColor(new Color(33, 33, 33, 0));
		overlay.setStrokeColor(Color.red);
		IJ.getImage().setOverlay(overlay);
	}

	// Unimplemented Methods
	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
	

	
	
	
	public static void main (String args[]) {
		AIF a = new AIF(new double[100]);
		//a.AIFMethodSelector();
		//boolean b = a.setAIFFromTxtFile(("C:\\Users\\pmacias\\Documents\\pruebas\\prueba.db"));
		
		
	}
}
