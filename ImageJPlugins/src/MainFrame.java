import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JTextField;


public class MainFrame extends JFrame {
	JComboBox comboFitting;
	boolean startPressed;
	JCheckBox sFit;
	JCheckBox showCont;
	JCheckBox AIFVoxels;
	JButton startButton;
	JTextField ThrField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 247);
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel mainPanel = new JPanel();
		tabbedPane.add(mainPanel, "Main");
		mainPanel.setLayout(null);
		
		comboFitting = new JComboBox();
		comboFitting.setMaximumRowCount(10);
		comboFitting.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
		comboFitting.setModel(new DefaultComboBoxModel(new String[] {"Auto", "NoFitter", "GammaFitter","autoGamma"}));
		comboFitting.setBounds(184, 11, 160, 20);
		mainPanel.add(comboFitting);
		
		JLabel lblSelectFitting = new JLabel("Select Fitting :");
		lblSelectFitting.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectFitting.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
		lblSelectFitting.setBounds(21, 14, 111, 14);
		mainPanel.add(lblSelectFitting);
		
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startPressed = true;
				
			}
		});
		startButton.setBounds(137, 77, 89, 23);
		mainPanel.add(startButton);
		
		 sFit = new JCheckBox("S-Fitting");
		 sFit.setSelected(true);
		 sFit.setEnabled(false);
		sFit.setBounds(6, 151, 97, 23);
		mainPanel.add(sFit);
		
		 showCont = new JCheckBox("Show Contrast w/ mouseMove.");
		showCont.setBounds(6, 125, 198, 23);
		mainPanel.add(showCont);
		
		JPanel tabAIF = new JPanel();
		tabbedPane.addTab("AIF ", null, tabAIF, null);
		tabAIF.setLayout(null);
		
		AIFVoxels = new JCheckBox("Show AIF voxels");
		AIFVoxels.setBounds(6, 151, 117, 23);
		tabAIF.add(AIFVoxels);
		
		JLabel lblRelativeThreshold = new JLabel("Rel. Threshold");
		lblRelativeThreshold.setBounds(10, 11, 69, 14);
		tabAIF.add(lblRelativeThreshold);
		
		ThrField = new JTextField();
		ThrField.setHorizontalAlignment(SwingConstants.CENTER);
		ThrField.setText("1");
		ThrField.setBounds(85, 11, 53, 14);
		tabAIF.add(ThrField);
		ThrField.setColumns(10);
	}
	
	public Object getCombo() {
		return comboFitting.getSelectedItem();
	}
}
