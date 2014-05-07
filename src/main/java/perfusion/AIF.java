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
	private List<VoxelT2> probAIFs;
	private List<VoxelT2> AIFValid;
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
		AIFValid = new ArrayList<VoxelT2>();
		for (VoxelT2 v : AllVoxels)
			// Make it maximum dependent
			if (v.AIFValid)
				AIFValid.add(v);

		probAIFs = MathAIF.getAIFs(AIFValid);
		if (!probAIFs.isEmpty())
			AIF = MathAIF.getAIF(probAIFs, true);
		else
			AIF = new double[AllVoxels.get(0).contrastRaw.length];
		
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

	/*
	 * public List<Voxel> getProbAIFsV() { return
	 * (List<Voxel>)(List<?>)probAIFs; }
	 */

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
		//manager.addWindowListener(this);
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

	/**
	 * Implements the way for calculating AIF from a list of {@link VoxelT2}
	 * 
	 * @param voxels
	 *            The {@link VoxelT2}
	 */
	public void manualCalc(List<VoxelT2> voxels) {
		manager.setName("AIF ROIs");
		manager.setVisible(true);
		JOptionPane jo = new JOptionPane(
				"Have you selected your own ROIs?\nSelect the ROIs you want to use"
						+ " and click OK", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION, null);

		jo.setEnabled(true);
		JDialog dialog2 = jo.createDialog("AIF Validation");

		dialog2.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog2.setVisible(true);
		AIF = MathAIF.getAIF(voxelsROI(voxels), true);
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

	/**
	 * Implements a way for getting the voxels from a {@link Roi} selected by
	 * the user using the {@link RoiManager}
	 * 
	 * @param allV
	 *            The whole set of voxels within the {@link ImagePlus} where the
	 *            user select the ROIs
	 * @return The voxels selected by the user
	 */
	public List<VoxelT2> voxelsROI(List<VoxelT2> allV) {
		List<VoxelT2> res = new ArrayList<VoxelT2>();
		manager.setName("AIF ROIs");
		// AIFSelect = manager;
		Roi[] rois = manager.getRoisAsArray();
		for (int i = 0; i < rois.length; i++) {
			//int z2 =rois[i].getPosition()+1;
			int z2 = manager.getSliceNumber(rois[i].getName());
			if (z2 == -1)
				z2 = 1;
			int fromIn = -1, toIn = -1;
			int[][] points = getPointsROI(rois[i]);

			if (points[0].length > 1) {// Point case
				// ///////////////////////////
				for (int j = 0; fromIn == -1 && j < points[0].length; j++)
					fromIn = allV.indexOf(VoxelT2.VoxelSearch(allV,
							points[0][j], points[1][j], z2));

				List<VoxelT2> subList = allV.subList(fromIn, allV.size() - 1);
				for (int j = points[0].length - 1; toIn == -1 && j > 0; j--)
					toIn = subList.indexOf(VoxelT2.VoxelSearch(subList,
							points[0][j], points[1][j], z2));
				// //////////////////////////////////////////

				subList = subList.subList(0, toIn);
				for (int j = 0; j < points[0].length; j++ /* Point p : points */) {

					VoxelT2 v = VoxelT2.VoxelSearch(subList, points[0][j],
							points[1][j], z2);
					if (v != null)
						res.add(v);
				}

			} else {
				VoxelT2 v = VoxelT2.VoxelSearch(allV, points[0][0],
						points[1][0], z2);
				if (v != null)
					res.add(v);
			}
		}
		//probAIFs = res;
		return res;

	}

	/**
	 * Obtains the coordinates from voxels inside a {@link Roi}
	 * 
	 * @param roi
	 * @return An array with the points inside the ROI
	 */
	public static int[][] getPointsROI(Roi roi) {
		// TODO line case
		int n = 0;
		Rectangle r = roi.getBounds();
		byte[] mask;

		if (roi.getType() == Roi.RECTANGLE) {
			mask = null;
			n = r.width * r.height;
		} else
			mask = (byte[]) (roi.getMask().getPixels());

		for (int j = 0; j < mask.length; j++) {
			if (mask[j] != 0)
				n++; // count points in mask
		}

		int[][] poin = new int[2][n];

		/*
		 * By the type is possible to know the Roi's shape,the multi-point ROI
		 * is considered as well as a ROI type so is neccesary to restrict and
		 * look for just one point
		 */
		if (roi.getType() == Roi.POINT && n == 1) {
			poin[0][0] = r.x;
			poin[1][0] = r.y;
		} else {

			int h = 0;
			int z = 0;
			for (int y = r.y; y < r.y + r.height; y++)
				for (int x = r.x; x < r.x + r.width; x++) {
					if (mask == null || (h < mask.length && mask[h] != 0)) {
						poin[0][z] = x;
						poin[1][z] = y;
						// pointRes[z] = new Point(x,y);
						z++;

					}
					h++;
				}
		}
		return poin;
	}
	
	/**
	 * Set the AIF with the specified values in the file. CSV format.
	 * 
	 * @param f source file. One line with the values
	 * @return true if everything goes well
	 */
	public boolean setAIFFromTxtFile(File f) {
		String csvLimit = ",";
		if (f.isFile()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String[] values = br.readLine().split(csvLimit);
				double[] probAIF = new double[values.length];
				for (int i = 0; i < values.length; i++) 
					probAIF[i] = Double.parseDouble(values[i]);
				
				setAIF(probAIF);
				return true; 
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return false;
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				return false;
			} /*finally {
				JOptionPane.showMessageDialog(new JFrame(),
					    "The introduced AIF is incorrect",
					    "AIF Error",
					    JOptionPane.ERROR_MESSAGE);
			}*/
		} else {
			return false;
		}
	}
	
	
	public boolean setAIFFromTxtFile(String s) {
		return setAIFFromTxtFile(new File(s));
	}

	public void windowClosed(WindowEvent e) {
		System.out.println("Closed");
		manager.removeAll();
		manager.close();
		manager = null;
	}
	
	public void AIFMethodSelector(final List <VoxelT2> voxels) {
	
				JPanel panel = new JPanel();
				panel.setLayout(null);
				JLabel label = new JLabel("Select your way");
				label.setBounds(71, 11, 77, 23);
				panel.add(label);
				 jf = new JFrame();
				jf.getContentPane().add(panel);
				
				JRadioButton AIFFromFile = new JRadioButton("AIF from a file");
				AIFFromFile.setBounds(6, 67, 124, 23);
				panel.add(AIFFromFile);
				
				final JRadioButton manualCalc = new JRadioButton("Manual Calculation");
				manualCalc.setBounds(6, 41, 124, 23);
				panel.add(manualCalc);
				final ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(manualCalc);
				buttonGroup.add(AIFFromFile);
				jf.setLocationRelativeTo(null);
				//jf.requestFocus();
			
				
				JButton continueBtn = new JButton("Continue...");
				continueBtn.addMouseListener(new MouseListener() {

					public void mouseClicked(MouseEvent arg0) {
						if (buttonGroup.getSelection() != null ) {
							System.out.println("tocado");
							if(manualCalc.isSelected()) {
								System.out.println("Manual Calc");
								manualCalc(voxels);
								jf.dispose();
							} else {
								System.out.println("Intro from file");
							}
						} else {
							System.out.println("No Tocado");
						}
						
					}
					public void mouseEntered(MouseEvent arg0) {}
					public void mouseExited(MouseEvent arg0) {}
					public void mousePressed(MouseEvent arg0) {}
					public void mouseReleased(MouseEvent arg0) {}
					
				});

				continueBtn.setBounds(113, 90, 89, 23);
				panel.add(continueBtn);
				jf.setSize(400, 200);
				//jf.pack();
				jf.setVisible(true);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
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
		boolean b = a.setAIFFromTxtFile(("C:\\Users\\pmacias\\Documents\\pruebas\\prueba.db"));
		
		
	}
}
