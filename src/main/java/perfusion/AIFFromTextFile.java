package perfusion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Set the AIF with the specified values in the file. CSV format.
 * 
 */
	public class AIFFromTextFile implements AIFCalculator {
		File f;
		public AIFFromTextFile (File f) {
			this.f = f;
		}
		
		public AIFFromTextFile(String f) {
			this(new File(f));
		}


		@Override
		public double[] doAIFCalculation() {
			String csvLimit = ",";
			if (f.isFile()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(f));
					String[] values = br.readLine().split(csvLimit);
					double[] probAIF = new double[values.length];
					for (int i = 0; i < values.length; i++) 
						probAIF[i] = Double.parseDouble(values[i]);
					
					return probAIF;
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				} /*finally {
					JOptionPane.showMessageDialog(new JFrame(),
						    "The introduced AIF is incorrect",
						    "AIF Error",
						    JOptionPane.ERROR_MESSAGE);
				}*/
			} else {
				return null;
			}
			return null;
			
			
		}
		
	}


