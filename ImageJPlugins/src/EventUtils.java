import ij.IJ;
import ij.ImagePlus;

import ij.gui.ImageCanvas;

import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;

import javax.swing.JCheckBox;

/**
 * Implements the mouse events and the overlay option
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Mac�as
 *         Gordaliza</a>
 * 
 */
public class EventUtils implements MouseListener, MouseMotionListener,
		WindowListener {
	ImageCanvas canvas;
	ImagePlus ip;
	PlotWindow pw;
	Plot chart;
	List<VoxelT2> voxels;
	JCheckBox showMove;

	/**
	 * Constructor, associates the event to a particular image and voxels
	 * 
	 * @param ip
	 *            Image to be asocciated
	 * @param voxels
	 *            in the image
	 * @param showMove
	 *            indicates if the voxel values are shown with the mouse
	 *            movement
	 */
	public EventUtils(ImagePlus ip, List<VoxelT2> voxels, JCheckBox showMove,
			Plot c) {
		this.ip = ip;
		canvas = ip.getCanvas();
		this.voxels = voxels;
		chart = c;
		pw = null;
		this.showMove = showMove;

	}

	/**
	 * Enable the events
	 */
	public void turnOn() {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

	}

	/**
	 * Unable the Events
	 */
	public void turnOff() {
		canvas.addMouseListener(null);
		canvas.addMouseMotionListener(null);
	}

	/**
	 * Performs the showing for a particular voxel,when this one is marked by
	 * the mouse
	 */
	public void mouseClicked(MouseEvent e) {
		ip = IJ.getImage();
		canvas = ip.getCanvas();
		int offscreenX = canvas.offScreenX(e.getX());
		int offscreenY = canvas.offScreenY(e.getY());
		VoxelT2 v = VoxelT2.VoxelSearch(voxels, offscreenX, offscreenY,
				ip.getSlice());

		if (v != null) {
			double[] x = new double[v.contrastRaw.length], y = new double[x.length];
			for (int i = 0; i < x.length; i++)
				x[i] = i;

			y = v.contrastRaw;

			Plot chart = new Plot("slice:" + ip.getSlice() + "  x:"
					+ offscreenX + "  y:" + offscreenY, "Time", "Contrast", x,
					y);
			if (pw == null) {
				pw = chart.show();
				pw.addWindowListener(this);

			} else

				pw.setTitle("slice:" + ip.getSlice() + "  x:" + offscreenX
						+ "  y:" + offscreenY);
			chart.setColor(java.awt.Color.BLUE);
			chart.addPoints(x, v.contrastFitted, PlotWindow.LINE);
			chart.addLabel(0.75, 0.2, "� Fitted Contrast");
			chart.setColor(java.awt.Color.BLACK);
			chart.addLabel(0.75, 0.1, "� Raw Contrast");

			pw.drawPlot(chart);
		} else if (showMove.isSelected() == false)
			IJ.showMessage("No meaningful contrast");

	}

	public void windowClosed(WindowEvent e) {
		pw = null;
	}

	/**
	 * When the option is selected,perform the mouseClicked action just with the
	 * mouse movement
	 */
	public void mouseMoved(MouseEvent e) {
		if (showMove.isSelected() == true)
			mouseClicked(e);

	}
	
	public static Overlay createOverlay (List<VoxelT2> points) {
		
		Overlay overlay = new Overlay();
		// getting points
		for (Voxel v : points) {
			PointRoi pr = new PointRoi(v.x, v.y);

			pr.setPosition(1, v.slice, 1);
			overlay.add(pr);

		}
		return overlay;
	}

	/**
	 * Displays the points for each {@link VoxelT2} coordinates over an image as
	 * a {@link Overlay}
	 * 
	 * @param notFit The {@link VoxelT2} list 
	 */
	public static void showPointsOverlays(List<VoxelT2> notFit) {
		
		ImagePlus imp = IJ.getImage();

		//Overlay overlay = new Overlay();
		Overlay overlay = createOverlay(notFit);
		// getting points
		/**
		for (Voxel v : notFit) {
			PointRoi pr = new PointRoi(v.x, v.y);

			pr.setPosition(1, v.slice, 1);
			overlay.add(pr);

		}**/

		overlay.setFillColor(new Color(33, 33, 33, 0));
		overlay.setStrokeColor(Color.red);
		imp.setOverlay(overlay);

	}

	//Unimplemented
	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

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

	public void mouseDragged(MouseEvent e) {
	}

}
