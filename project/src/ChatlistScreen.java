import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ChatlistScreen extends JFrame{
	private JButton undo;
	private JButton plus;
	private MainScreen mainScreen;
	private String userId;
	
	public ChatlistScreen(MainScreen mainScreen, String userId) {
		this.mainScreen = mainScreen;
		this.userId = userId;
		setTitle("ChatlistScreen");
		
		buildGUI();

		setSize(400, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setVisible(true);
	}
	
	private void buildGUI() {
		add(createTopPanel(), BorderLayout.NORTH);
		//add(createCenterPanel(), BorderLayout.CENTER);
	}
	
	private JPanel createTopPanel() {
		JPanel p = new JPanel(new GridLayout(2,0));
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p1.setBackground(Color.WHITE);

		undo = new JButton("◀");
		undo.setBackground(Color.white);
		undo.setFocusPainted(false);
		undo.setBorderPainted(false);
		undo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				
			}
		});
		
		JLabel user_name = new JLabel(userId); //임시(실제로는 id창에서 받은 이름(t_id)으로 사용)

		p1.add(undo);
		p1.add(user_name);
		
		JPanel p2 = new JPanel(new BorderLayout());
		p2.setBackground(Color.WHITE);
		
		JLabel message = new JLabel("    메시지");
		
		plus = new JButton("➕");
		plus.setBackground(Color.white);
		plus.setFocusPainted(false);
		plus.setBorderPainted(false);
		plus.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				
			}
		});
		
		p2.add(message, BorderLayout.WEST);
		p2.add(plus, BorderLayout.EAST);
		
		p.add(p1);
		p.add(p2);
		
		return p;
	}
	
	private JPanel createCenterPanel() {
		JPanel p = new JPanel();
		p.setBackground(Color.white);
		
		return p;
	}

	

}
