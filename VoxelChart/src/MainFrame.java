import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;


public class MainFrame extends JFrame {
	public final JButton btnGo;
	JCheckBox chckboxInvertValues;
	private JPanel contentPane;

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
		setTitle("Dynamic Pixel Chart");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 288, 94);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		btnGo = new JButton("Go");
		btnGo.setBounds(10, 11, 89, 23);
		contentPane.add(btnGo);
		
		chckboxInvertValues = new JCheckBox("Invert values");
		chckboxInvertValues.setBounds(139, 11, 97, 23);
		contentPane.add(chckboxInvertValues);
		
		JLabel lblPressCtrl = new JLabel("Press 'q' for enable/disb. continous showing");
		lblPressCtrl.setFont(new Font("Tahoma", Font.PLAIN, 8));
		lblPressCtrl.setBounds(20, 42, 216, 14);
		contentPane.add(lblPressCtrl);
	}
}
