import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Plot;
import ij.gui.PlotWindow;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JCheckBox;


public class EventUtils implements MouseListener ,MouseMotionListener,WindowListener{
	ImageCanvas canvas;
	ImagePlus ip;
	PlotWindow pw;
	List<VoxelT2> voxels;
	JCheckBox showMove;
	
	/**
	 * Constructor, associates the event to a particular image and voxels
	 * @param ip Image to be asocciated
	 * @param voxels in the image
	 * @param showMove indicates if the voxel values are shown with the mouse movement
	 */
	public EventUtils (ImagePlus ip,List<VoxelT2> voxels,JCheckBox showMove) {
		this.ip = ip;
		canvas = ip.getCanvas();
		this.voxels = voxels;
		pw = null;
		this.showMove = showMove;
		
	}
	
	/**
	 *  Enable the events*/
	public void turnOn() {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
	
	}
	
	/**
	 *  Unable the Events */
	public void turnOff() {
		canvas.addMouseListener(null);
		canvas.addMouseMotionListener(null);
	}

	/**
	 * Performs the showing for a particular voxel,when this one is marked by the mouse
	 */
	public void mouseClicked(MouseEvent e) {
		int offscreenX = canvas.offScreenX(e.getX());
		int offscreenY = canvas.offScreenY(e.getY());
		VoxelT2 v = VoxelT2.VoxelSearch(voxels, offscreenX, offscreenY, ip.getSlice());
		
		if(v != null){
			double[] x = new double[v.contrastRaw.length], y = new double[x.length];
			for (int i=0; i < x.length; i++)
				x[i] = i;
			
			y = v.contrastRaw;
			
		Plot chart = new Plot("slice:"+ip.getSlice()+"  x:"+offscreenX+"  y:"+offscreenY ,"Time","Contrast",x,y);
		if (pw==null){
			pw = chart.show();
			pw.addWindowListener(this);
		  
		}else
			pw.setTitle("slice:"+ip.getSlice()+"  x:"+offscreenX+"  y:"+offscreenY);
			chart.setColor(java.awt.Color.BLUE);
			chart.addPoints(x, v.contrastFitted, PlotWindow.LINE);
			chart.addLabel(0.75, 0.2, "— Fitted Contrast");
			chart.setColor(java.awt.Color.BLACK);
			chart.addLabel(0.75, 0.1, "— Raw Contrast");
			pw.drawPlot(chart);
		} else 
			if(showMove.isSelected() == false)
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
	 * When the option is selected,perform the mouseClicked action 
	 * just with the mouse movement
	 */
	public void mouseMoved(MouseEvent e) {
		if(showMove.isSelected() == true)
		mouseClicked(e);
		
	}

}
