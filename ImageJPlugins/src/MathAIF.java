import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class MathAIF {

	/**
	 * 
	 * @param function
	 *            the one from we can get the parameter MC,MMC and FWHM
	 * @return an array with the required parameters, where result[0] -> MC;
	 *         result[1] -> MMC; result[2] -> FWHM
	 */
	public static double[] parameters(double[] function) {
		double[] result = new double[3];
		double MC = StatUtils.max(function);
		int MMC = MathUtils.whereIs(function, MC);
		result[0] = MC;
		result[1] = MMC;
		result[2] = getFWHM(function,MC,MMC);
	
		
	

		return result;
	}
	
	public static double getFWHM (double[] f,double MC,int MMC) {
		int t1 = MMC - 1, t2 = MMC + 1;
		double result;
		try {
			while (f[t1] >= MC / 2)
				if (f[t1] <= f[t1 + 1]	|| f[t1 - 1] <= f[t1])
					t1--;
				else
					t1 = -1;

			while (f[t2] >= MC / 2)
				if (f[t2] <= f[t2 - 1]	|| f[t2 + 1] <= f[t2])
					t2++;
				else
					t2 = f.length;

			result=  t2 - t1;

		} catch (ArrayIndexOutOfBoundsException e) { /*
													 * Just in case is not
													 * possible find out FWHM
													 */
			result =  Double.NaN;
		}
		return result;
	}

	// TODO hacerlo general
	public static List<VoxelT2> getAIFs(List<VoxelT2> voxels) {
		int dim = voxels.size();
		double[] MCs, FWHMs, MMCs = new double[dim];
		MCs = new double[dim];
		FWHMs = new double[dim];
		for (int i = 0; i < voxels.size(); i++) {
		
			MCs[i] =  voxels.get(i).getMC();
			MMCs[i] =  voxels.get(i).getMMC();
			FWHMs[i] =  voxels.get(i).getFWHM();
		

		}
		System.out.println(StatUtils.max(MCs)+" "+MathUtils.whereIs(MCs, StatUtils.max(MCs)));
		
		int[] biggerThanHMaxMC = MathUtils.findBiggerThan(MCs,
				StatUtils.max(MCs) * 0.0);

		boolean[] probAIF = isAIF(biggerThanHMaxMC, MMCs, FWHMs,MCs);
		 List<VoxelT2> posAIFs = new ArrayList<VoxelT2>();
		for (int i = 0; i < probAIF.length; i++)
			if (probAIF[i] == true){
				posAIFs.add(voxels.get(biggerThanHMaxMC[i]));
				voxels.get(biggerThanHMaxMC[i]).AIFValid = true;
				
			}

		return posAIFs;
		/*Vector<Voxel> bacala = new Vector<Voxel>();
		bacala.add(posAIFs.get(17));
		return bacala;*/

	}
	
	
	public static double[] getAIF(List<VoxelT2> voxels,boolean meaningVoxels) {	
		if (meaningVoxels==true) 
			return stimAIF(voxels);
		else
		return stimAIF(getAIFs(voxels));
		
	}
	
	
    

	/**
	 * 
	 * @param biggerThanHMaxMC
	 *            indices where the MCs are bigger than max(MC)/2
	 * @param MMCs
	 *            all the Maximum Concentration Moments
	 * @param FWHMs
	 *            all the Full Width Half Medium
	 * @return a boolean where the curves accomplish the conditions for being
	 *         AIF, thus FWHM(AIF) = mean(FWHM) - 1.5 standard deviation (FWHM)
	 */
	private static boolean[] isAIF(int[] biggerThanHMaxMC, double[] MMCs,
			double[] FWHMs,double[] MCs) {
		double maxMC = 0;
		boolean anyCoincidence = false;
		double meanMMC = StatUtils.mean(MMCs);
		double thrMMC = meanMMC - 1.5
				* FastMath.sqrt(StatUtils.variance(MMCs, meanMMC));
		double meanFWHM = StatUtils.mean(FWHMs);
		double thrFWHM = meanFWHM - 1.5
				* FastMath.sqrt(StatUtils.variance(FWHMs, meanFWHM));

		boolean[] probAIF = new boolean[biggerThanHMaxMC.length];
		int j = 0;
		for (int i : biggerThanHMaxMC) {
			if (FWHMs[i] >= thrFWHM-1 && FWHMs[i] <=  (thrFWHM+1 ) &&  MMCs[i] >=  thrMMC-1 &&  MMCs[i] <=  thrMMC+1) {
				
				probAIF[j] = true;
				anyCoincidence = true;
				
				if(MCs[i] > maxMC)
					maxMC = MCs[i];
				
			}

			j++;
		}
		
		for(int i = 0; i < probAIF.length; i++)
			if(probAIF[i] == true && MCs[i] < 0.75 * maxMC)
				probAIF[i] = false;
				
		
		/*if (anyCoincidence == false)
			for (int i =0; i < probAIF.length; i++)
				probAIF[i] = true;*/
		
		return probAIF;

	}

	private static double[] stimAIF(List<VoxelT2> posAIFs) {
		double[] result = new double[(posAIFs.get(0)).contrastRaw.length];
		
		for (int i = 0; i < result.length; i++) {
			double[] insContrastRaw = new double[posAIFs.size()];
			for (int j = 0; j < posAIFs.size(); j++)
				insContrastRaw[j] = (posAIFs.get(j)).contrastFitted[i];

			result[i] = MathUtils.mEsti(insContrastRaw);
			//if (result[i] < 0) result[i] = 0;
		}
		return result;
	}

}
