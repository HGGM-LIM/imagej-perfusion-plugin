package perfusion;


import java.util.List;

public class AIFAutomaticCalculation implements AIFCalculator {
	List<VoxelT2> AllVoxels;
	AIF aif;
	
	public AIFAutomaticCalculation(AIF aif, List<VoxelT2> AllVoxels) {
		this.AllVoxels = AllVoxels;
		this.aif = aif;
		
	}
	
	@Override
	public double[] doAIFCalculation() {
		if (!aif.probAIFs.isEmpty())
			return MathAIF.getAIF(aif.probAIFs, true);
		else
			return  new double[AllVoxels.get(0).contrastRaw.length];
		
	}

}
