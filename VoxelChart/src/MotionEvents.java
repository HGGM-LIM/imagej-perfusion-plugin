import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;
import ij.gui.PlotWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

public class MotionEvents implements MouseListener, MouseMotionListener,
		WindowListener, KeyListener {
	private ImageCanvas canvas;
	private ImagePlus ip;
	private PlotWindow pw;
	private MainFrame assFr;
	private boolean moveFlag;
	// Plot chart;
	// List<Voxel> voxels;
	private Voxel[][][] voxelsArr;

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
	public MotionEvents(ImagePlus ip, List<Voxel> voxels, MainFrame assFr) {

		new MotionEvents(ip, (Voxel[][][]) voxels.toArray(), assFr);

	}

	public MotionEvents(ImagePlus ip, Voxel[][][] voxels, MainFrame assFr) {
		this.ip = ip;
		canvas = ip.getCanvas();
		voxelsArr = voxels;
		pw = null;
		this.assFr = assFr;
		moveFlag = true;

	}

	/**
	 * Enable the events
	 */
	public void turnOn() {
		
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		
		canvas.addKeyListener(this);
		
		
		

	}

	/**
	 * Unable the Events
	 */
	public void turnOff() {
		canvas.removeMouseMotionListener(this);
		canvas.removeMouseListener(this);
		canvas.removeKeyListener(this);
		
		
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
		Voxel v = voxelsArr[offscreenX][offscreenY][ip.getSlice() - 1];

		if (v != null) {
			double[] x = new double[v.tac.length], y = new double[x.length];
			for (int i = 0; i < x.length; i++)
				x[i] = i;

			if (!assFr.chckboxInvertValues.isSelected())
				y = v.tac;
			else
				for (int i = 0; i < y.length; i++)
					y[i] = -v.tac[i];

			Plot chart = new Plot("slice:" + ip.getSlice() + "  x:"
					+ offscreenX + "  y:" + offscreenY, "Frame", "Inten", x, y);
			if (pw == null) {
				pw = chart.show();
				pw.addWindowListener(this);

			} else

				pw.setTitle("slice:" + ip.getSlice() + "  x:" + offscreenX
						+ "  y:" + offscreenY);
			chart.addPoints(x, v.tac, PlotWindow.LINE);

			pw.drawPlot(chart);
		} else
			IJ.showMessage("No meaningful contrast");

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

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
		pw = null;

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

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * When the option is selected,perform the mouseClicked action just with the
	 * mouse movement
	 */
	public void mouseMoved(MouseEvent e) {
		if (moveFlag == true)
			mouseClicked(e);
			

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	/*public synchronized void keyPressed(KeyEvent e) { moveFlag = !moveFlag; }*/
	@Override
	public void keyPressed(KeyEvent e) {
		// Catch the event for enables or disables the chart's show with the
		// mouse movement
		if (e.getKeyCode() == KeyEvent.VK_Q){
			// System.out.println();
			IJ.showMessage("olé");
			moveFlag = !moveFlag;
			IJ.showMessage("cabrón");
			canvas.repaint();
		
		}

	}

	
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		

	}

}
