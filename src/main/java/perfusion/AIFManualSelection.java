package perfusion;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.Rectangle;
import java.awt.Dialog.ModalityType;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Implements the way for calculating AIF from a list of {@link VoxelT2}
 * @param voxels   The {@link VoxelT2}
 */

public class AIFManualSelection  implements AIFCalculator {
	AIF aif;
	List<VoxelT2> voxels;
	
	public AIFManualSelection(AIF aif, List<VoxelT2> voxels) {
		this.aif = aif;
		this.voxels = voxels;
	}
	
	
	@Override
	public double[] doAIFCalculation() {
		aif.manager.setName("AIF ROIs");
		aif.manager.setVisible(true);
		JOptionPane jo = new JOptionPane(
				"Have you selected your own ROIs?\nSelect the ROIs you want to use"
						+ " and click OK", JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION, null);

		jo.setEnabled(true);
		JDialog dialog2 = jo.createDialog("AIF Validation");

		dialog2.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog2.setVisible(true);
		return MathAIF.getAIF(voxelsROI(voxels), true);
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
	private List<VoxelT2> voxelsROI(List<VoxelT2> allV) {
		List<VoxelT2> res = new ArrayList<VoxelT2>();
		aif.manager.setName("AIF ROIs");
		// AIFSelect = manager;
		Roi[] rois = aif.manager.getRoisAsArray();
		for (int i = 0; i < rois.length; i++) {
			//int z2 =rois[i].getPosition()+1;
			int z2 = aif.manager.getSliceNumber(rois[i].getName());
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
	private static int[][] getPointsROI(Roi roi) {
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


}
