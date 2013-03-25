import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class MainFrame extends JFrame {
	JComboBox comboFitting;
	boolean startPressed;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 247);
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel mainPanel = new JPanel();
		tabbedPane.add(mainPanel, "Main");
		mainPanel.setLayout(null);
		
		comboFitting = new JComboBox();
		comboFitting.setMaximumRowCount(10);
		comboFitting.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
		comboFitting.setModel(new DefaultComboBoxModel(new String[] {"Auto", "NoFitter", "GammaFitter"}));
		comboFitting.setBounds(184, 11, 160, 20);
		mainPanel.add(comboFitting);
		
		JLabel lblSelectFitting = new JLabel("Select Fitting :");
		lblSelectFitting.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectFitting.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 11));
		lblSelectFitting.setBounds(21, 14, 111, 14);
		mainPanel.add(lblSelectFitting);
		
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startPressed = true;
			}
		});
		startButton.setBounds(137, 77, 89, 23);
		mainPanel.add(startButton);
		
		JPanel tabAIF = new JPanel();
		tabbedPane.addTab("AIF ", null, tabAIF, null);
	}
	
	public Object getCombo() {
		return comboFitting.getSelectedItem();
	}
}
