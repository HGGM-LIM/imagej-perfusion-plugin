import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import ij.IJ;
import ij.ImagePlus;

import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


/**
 * <p>
 * Instant Voxel Chart ImageJ plugin.
 * </p>
 * 
 * <p>
 * This plugin allows the user for watching the intensity/frame curve for each
 * voxel within the selected slice with the mouse motion. The plugin works
 * properly over 4D or 3D images but the user must be careful choosing which are
 * frames and slices!
 * </p>
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Macías
 *         Gordaliza</a>.
 * 
 */

public class Dynamic_Pixel_Chart implements PlugInFilter, ActionListener,
		WindowListener {

	ImagePlus ip;
	Voxel[][][] allVoxels;
	MainFrame mf;
	MotionEvents me;
	

	public void run(ImageProcessor arg0) {

		mf = new MainFrame();
		mf.btnGo.addActionListener(this);
		mf.addWindowListener(this);
		mf.setVisible(true);
		
	
	}

	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		ip = arg1;
		return DOES_ALL + STACK_REQUIRED;
	}

	/**
	 * Implements an effective main
	 */
	public void doProcess() {
		// Creates an extended ImagePlus for hyperstacks
		ImagePlusHyp myHyp = new ImagePlusHyp(ip);
		int[] dim = ip.getDimensions();
		// allVoxels arrays keep each voxel in the hyperstack so the size is
		// fixed
		allVoxels = new Voxel[dim[0]][dim[1]][dim[3]];
		ImagePlusHypIterator iphi = new ImagePlusHypIterator(myHyp);
		while (iphi.hasNext()) {
			Voxel v = iphi.next();
			allVoxels[v.x][v.y][v.slice - 1] = v;
			IJ.showStatus("Adding voxels...");
		}
		IJ.setSlice(1);
		IJ.showStatus("Ready");
		// Creates and establish the events
		me = new MotionEvents(ip, allVoxels, mf);
		me.turnOn();

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		doProcess();

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// Disable the events and establish allvoxels as an empty array when the
		// mainFrame is closed
		me.turnOff();
		allVoxels = null;

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

}