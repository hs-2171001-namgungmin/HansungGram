import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class ChatScreen extends JFrame {
	private JButton undo;
	private JTextField t_input;
	private JButton b_image, b_emoji;
	private JTextPane t_display;
	private DefaultStyledDocument document;

	public ChatScreen() {
		super("ChatScreen");

		buildGUI();

		setSize(400, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setVisible(true);
	}

	private void buildGUI() {
		add(createTopPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);
		add(createInputPanel(), BorderLayout.SOUTH);
	}

	private JPanel createTopPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p1.setBackground(Color.WHITE);

		undo = new JButton("◀");
		undo.setBackground(Color.white);
		undo.setFocusPainted(false);
		undo.setBorderPainted(false);

		JLabel user_name = new JLabel("HJ"); // 임시(실제로는 ChatlistScreen창에서 클릭한 채팅목록 이름으로 사용)

		p1.add(undo);
		p1.add(user_name);

		JPanel p2 = new JPanel(new BorderLayout());
		p2.setBackground(Color.WHITE);

		JLabel message = new JLabel("    메시지");
		message.setFont(new Font("", Font.PLAIN, 10));

		p2.add(message, BorderLayout.WEST);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.GRAY);

		p.add(p1);
		p.add(separator);
		p.add(p2);

		return p;
	}

	private JPanel createCenterPanel() {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(Color.white);

		document = new DefaultStyledDocument();
		t_display = new JTextPane(document);

		t_display.setEditable(false);

		p.add(new JScrollPane(t_display), BorderLayout.CENTER);

		return p;
	}

	private JPanel createInputPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBackground(Color.white);

		t_input = new JTextField(30);
		t_input.setBorder(null); // 테두리 삭제
		t_input.setBackground(Color.LIGHT_GRAY);
		t_input.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});

		ImageIcon emoji = new ImageIcon("emoji.png");
		Image img = emoji.getImage();
		Image newImg = img.getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH);

		b_emoji = new JButton(new ImageIcon(newImg));
		b_emoji.setPreferredSize(new Dimension(28, 28));
		b_emoji.setBackground(Color.white);
		b_emoji.setFocusPainted(false);
		b_emoji.setBorderPainted(false);
		b_emoji.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = new JDialog((Frame) null, "Select an Emoji", true);
		        dialog.setLayout(new GridLayout(4, 3));
		        
		        for(int i = 1; i<= 12; i++) {
		        	ImageIcon icon = new ImageIcon("emoji" + i + ".png");
		        	Image reicon = icon.getImage();
		        	Image newreicon = reicon.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
		        	
		        	ImageIcon icon2 = new ImageIcon(newreicon);
		        	
		        	JButton emoButton = new JButton(icon2);
		        	emoButton.setBackground(Color.white);
		        	emoButton.setFocusPainted(false);
		        	emoButton.setBorderPainted(false);
		        	
		        	emoButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							ImageIcon selectedIcon = (ImageIcon) emoButton.getIcon();
							printDisplay(selectedIcon);
							
							dialog.dispose();
						}
		        	});
		        	dialog.add(emoButton);
		        }
				
		        dialog.setSize(250, 250);
		        dialog.setVisible(true);
			}
			
		});

		ImageIcon image = new ImageIcon("File plus.png");
		Image img2 = image.getImage();
		Image newImg2 = img2.getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH);

		b_image = new JButton(new ImageIcon(newImg2));
		b_image.setPreferredSize(new Dimension(28, 28));
		b_image.setBackground(Color.white);
		b_image.setFocusPainted(false);
		b_image.setBorderPainted(false);
		b_image.addActionListener(new ActionListener() {

			JFileChooser chooser = new JFileChooser();
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"JPG & GIF & PNG Images", //파일 이름에 창에 출력될 문자열
						"jpg", "gif", "png"); //파일 필터로 사용되는 확장자
				
				chooser.setFileFilter(filter);
				
				int ret = chooser.showOpenDialog(ChatScreen.this);
				if(ret != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(ChatScreen.this, "파일을 선택하지 않았습니다.");
					return;
				}
				
				t_input.setText(chooser.getSelectedFile().getAbsolutePath());
				sendImage();
				
			}
			
		});

		p.add(t_input);
		p.add(b_emoji);
		p.add(b_image);

		return p;
	}

	private void printDisplay(String msg) {
		int len = t_display.getDocument().getLength();

		try {
			document.insertString(len, msg + "\n", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		t_display.setCaretPosition(len);
	}
	
	private void printDisplay(ImageIcon icon) {
		t_display.setCaretPosition(t_display.getDocument().getLength());
		
		if(icon.getIconWidth() > 400) {
			Image img = icon.getImage();
			Image changeImg = img.getScaledInstance(300,-1, Image.SCALE_SMOOTH);
			icon = new ImageIcon(changeImg);
		}
		
		t_display.insertIcon(icon);
		
		printDisplay("");
		t_input.setText("");
	}

	private void sendMessage() {
		String message = t_input.getText();
		if (message.isEmpty())
			return;
		
		printDisplay(message);

		t_input.setText("");
	}
	
	private void sendImage() {
		String filename = t_input.getText().strip();
		if(filename.isEmpty()) return;
		
		File file = new File(filename);
		if(!file.exists()) {
			printDisplay(">>파일이 존재하지 않습니다: " + filename);
			return;
		}
		
		ImageIcon icon = new ImageIcon(filename);
		
		printDisplay(icon);
		
		t_input.setText("");
	}

	public static void main(String[] args) {
		new ChatScreen();

	}

}
