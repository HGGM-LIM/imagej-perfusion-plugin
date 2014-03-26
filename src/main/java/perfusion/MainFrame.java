package perfusion;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.UnsupportedLookAndFeelException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Toolkit;
import javax.swing.BoxLayout;


/**
 * Application Window
 * 
 * @author <a href="mailto:pedro.macias.gordaliza@gmail.com">Pedro Mac�as Gordaliza</a>
 *
 */
public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3008152621433346520L;
	protected JComboBox comboFitting;
	private boolean startPressed;
	protected JCheckBox AIFVoxels;
	protected JButton startButton;
	protected JTextField ThrField;
	protected JCheckBox showCont;
	private JCheckBox sFit;
	private JPanel panel;
	private JTextField textField;

	/**
	 * Launch the application.
	 * @param args 
	 */
	public static void main(String[] args) {
		try {
            // Set cross-platform Java L&F (also called "Metal")
        UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		} 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\Images\\BIIG.png"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 247);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel mainPanel = new JPanel();
		tabbedPane.add(mainPanel, "Main");
		mainPanel.setLayout(null);
		
		comboFitting = new JComboBox();
		comboFitting.setMaximumRowCount(10);		
		comboFitting.setModel(new DefaultComboBoxModel(new String[] {"NoFitter", "GammaFitterACM", "GammaFitterSVD"}));
		comboFitting.setBounds(184, 11, 160, 20);
		mainPanel.add(comboFitting);
		
		JLabel lblSelectFitting = new JLabel("Select Fitting :");
		lblSelectFitting.setHorizontalAlignment(SwingConstants.CENTER);		
		lblSelectFitting.setBounds(21, 14, 111, 14);
		mainPanel.add(lblSelectFitting);
		
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startPressed = true;
				
			}
		});
		startButton.setBounds(137, 108, 89, 23);
		mainPanel.add(startButton);
		
		JPanel tabAIF = new JPanel();
		tabbedPane.addTab("Options", null, tabAIF, null);
		tabAIF.setLayout(null);
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Show Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(59, 109, 221, 72);
		panel.setToolTipText("Visual Options");
		tabAIF.add(panel);
		panel.setLayout(null);
		
		AIFVoxels = new JCheckBox("Show AIF voxels");
		AIFVoxels.setSelected(true);
		AIFVoxels.setBounds(6, 42, 209, 23);
		AIFVoxels.setToolTipText("Show the voxels used for the AIF calculation");
		panel.add(AIFVoxels);
		
		showCont = new JCheckBox("Show Contrast w/ mouse motion");
		showCont.setBounds(6, 16, 209, 23);
		showCont.setToolTipText("Show the Contrast-Curve with and without fitting");
		panel.add(showCont);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(59, 14, 221, 62);
		tabAIF.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblRelativeThreshold = new JLabel("Rel. Threshold");
		lblRelativeThreshold.setToolTipText("Realtive value for masking the background");
		lblRelativeThreshold.setBounds(6, 16, 121, 14);
		panel_1.add(lblRelativeThreshold);
		
		ThrField = new JTextField();
		ThrField.setBounds(133, 16, 34, 14);
		panel_1.add(ThrField);
		ThrField.setHorizontalAlignment(SwingConstants.CENTER);
		ThrField.setText("1");
		ThrField.setColumns(10);
		
		textField = new JTextField();
		textField.setToolTipText("0 to 1");
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setText("0.1");
		textField.setBounds(133, 41, 34, 14);
		panel_1.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Force-Fitting");
		lblNewLabel.setToolTipText("% of frames when the normal fit is not possible");
		lblNewLabel.setBounds(6, 41, 95, 14);
		panel_1.add(lblNewLabel);
		
		sFit = new JCheckBox("S-Fitting");
		sFit.setEnabled(false);
		sFit.setSelected(true);
		sFit.setVisible(false);
		sFit.setBounds(6, 83, 97, 23);
		tabAIF.add(sFit);
	}
	
	private Object getCombo() {
		return comboFitting.getSelectedItem();
	}
}
