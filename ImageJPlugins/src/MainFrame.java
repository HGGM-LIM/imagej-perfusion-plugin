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
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Toolkit;


public class MainFrame extends JFrame {
	JComboBox<?> comboFitting;
	boolean startPressed;
	JCheckBox AIFVoxels;
	JButton startButton;
	JTextField ThrField;
	JCheckBox showCont;
	 JCheckBox sFit;
	 private JPanel panel;
	 private JTextField textField;

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
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Mikel\\Documents\\ProyectoDoc\\Images\\BIIG.png"));
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
		comboFitting.setModel(new DefaultComboBoxModel(new String[] {"GammaFitterACM", "GammaFitterSVD", "NoFitter"}));
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
		startButton.setBounds(137, 108, 89, 23);
		mainPanel.add(startButton);
		
		JPanel tabAIF = new JPanel();
		tabbedPane.addTab("Options", null, tabAIF, null);
		tabAIF.setLayout(null);
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Show Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(59, 109, 210, 72);
		panel.setToolTipText("Visual Options");
		tabAIF.add(panel);
		panel.setLayout(null);
		
		AIFVoxels = new JCheckBox("Show AIF voxels");
		AIFVoxels.setBounds(6, 42, 117, 23);
		AIFVoxels.setToolTipText("Show the voxels used for the AIF calculation");
		panel.add(AIFVoxels);
		
		showCont = new JCheckBox("Show Contrast w/ mouseMove.");
		showCont.setBounds(6, 16, 198, 23);
		showCont.setToolTipText("Show the Contrast-Curve with and without fitting");
		panel.add(showCont);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(78, 14, 191, 62);
		tabAIF.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblRelativeThreshold = new JLabel("Rel. Threshold");
		lblRelativeThreshold.setToolTipText("Realtive value for masking the background");
		lblRelativeThreshold.setBounds(6, 16, 69, 14);
		panel_1.add(lblRelativeThreshold);
		
		ThrField = new JTextField();
		ThrField.setBounds(91, 16, 34, 14);
		panel_1.add(ThrField);
		ThrField.setHorizontalAlignment(SwingConstants.CENTER);
		ThrField.setText("1");
		ThrField.setColumns(10);
		
		textField = new JTextField();
		textField.setToolTipText("0 to 1");
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setText("0.1");
		textField.setBounds(91, 41, 34, 14);
		panel_1.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Force-Fitting");
		lblNewLabel.setToolTipText("% of frames when the normal fit is not possible");
		lblNewLabel.setBounds(6, 41, 66, 14);
		panel_1.add(lblNewLabel);
		
		sFit = new JCheckBox("S-Fitting");
		sFit.setEnabled(false);
		sFit.setSelected(true);
		sFit.setVisible(false);
		sFit.setBounds(6, 83, 97, 23);
		tabAIF.add(sFit);
	}
	
	public Object getCombo() {
		return comboFitting.getSelectedItem();
	}
}
