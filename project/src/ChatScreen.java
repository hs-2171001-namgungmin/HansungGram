import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Random;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;

public class ChatScreen extends JFrame {
	private JButton undo;
	private JTextField t_input;
    private JTextPane t_display;
    private JButton b_image, b_emoji;
    private DefaultStyledDocument document;
    private String userId;
    private String otherUserId = "Friend"; // 상대방 ID (임시)
    private String lastSender = ""; // 마지막 메시지 보낸 사용자

    public ChatScreen(String chatRoomName, String userId) {
        super("Chat with " + chatRoomName);
        this.userId = userId;

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
        undo.addActionListener(e -> dispose());
        undo.setFocusPainted(false);
        undo.setBorderPainted(false);

        JLabel userName = new JLabel(userId);
               
        p1.add(undo);
        p1.add(userName);

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
        t_input.setBorder(null); //테두리 삭제
        t_input.setBackground(Color.LIGHT_GRAY);
        t_input.addActionListener(e -> {
            String message = t_input.getText();
            if (!message.isEmpty()) {
                displayMessage(message, true);
                t_input.setText("");
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

		        for (int i = 1; i <= 12; i++) {
		            ImageIcon icon = new ImageIcon("emoji" + i + ".png");
		            Image reicon = icon.getImage();
		            Image newreicon = reicon.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);

		            ImageIcon resizedIcon = new ImageIcon(newreicon);

		            JButton emoButton = new JButton(resizedIcon);
		            emoButton.setBackground(Color.white);
		            emoButton.setFocusPainted(false);
		            emoButton.setBorderPainted(false);

		            emoButton.addActionListener(event -> {
		                dialog.dispose();
		                printDisplay(resizedIcon, true); // 이모티콘 출력
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
    private void displayMessage(String message, boolean isUser) {
        String sender = isUser ? userId : otherUserId;

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);

        JPanel profileAndBubblePanel = new JPanel(new BorderLayout());
        profileAndBubblePanel.setOpaque(false);
        profileAndBubblePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 여백 추가

        // 프로필 표시 (이전 사용자와 다를 때만)
        if (!sender.equals(lastSender)) {
            JLabel profileLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(getRandomColor(sender));
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                }
            };
            profileLabel.setPreferredSize(new Dimension(30, 30));
            profileAndBubblePanel.add(profileLabel, isUser ? BorderLayout.EAST : BorderLayout.WEST);
        }

        JLabel bubbleLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isUser ? Color.BLUE : Color.LIGHT_GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };

     // 텍스트 길이에 따라 다르게 처리
        if (message.length() < 20) {
            // 20자 미만: 폭만 조정 (줄바꿈 없음)
            bubbleLabel.setText("<html><p style='padding: 10px; white-space: nowrap;'>" + message + "</p></html>");
            bubbleLabel.setPreferredSize(new Dimension(message.length() * 10 + 30, 40)); // 폭과 고정 높이
        } else {
            // 20자 이상: 폭 제한 및 줄바꿈 처리
            bubbleLabel.setText("<html><div style='width: 180px; padding: 10px; word-wrap: break-word; white-space: normal;'>" + message + "</div></html>");
            
            // 동적 높이 계산
            int height = calculateHeight(message, bubbleLabel.getFontMetrics(bubbleLabel.getFont()));
            bubbleLabel.setPreferredSize(new Dimension(220, height)); // 폭 제한 + 동적 높이
        }

        bubbleLabel.setForeground(Color.WHITE);
        bubbleLabel.setOpaque(false);

        profileAndBubblePanel.add(bubbleLabel, BorderLayout.SOUTH);
        messagePanel.add(profileAndBubblePanel, isUser ? BorderLayout.EAST : BorderLayout.WEST);

        try {
            int len = document.getLength();
            document.insertString(len, "\n", null);
            t_display.setCaretPosition(len);
            t_display.insertComponent(messagePanel);

            // 마지막 보낸 사용자 업데이트
            lastSender = sender;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private int calculateHeight(String message, FontMetrics metrics) {
        int maxWidth = 200; // 말풍선 최대 폭
        int lineHeight = metrics.getHeight();
        int textWidth = metrics.stringWidth(message);

        // 텍스트의 줄 수 계산
        int lines = Math.max(1, (textWidth / maxWidth) + 1);

        return lines * lineHeight + 20; // 줄 수에 따른 높이 계산 (패딩 포함)
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
	private void printDisplay(ImageIcon icon, boolean isUser) {
	    JPanel emojiPanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT)); // 사용자 메시지 오른쪽 정렬, 상대방은 왼쪽 정렬
	    emojiPanel.setOpaque(false);

	    JLabel emojiLabel = new JLabel(icon);

	    // 이미지 크기 조정
	    if (icon.getIconWidth() > 400) {
	        Image img = icon.getImage();
	        Image resizedImg = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
	        icon = new ImageIcon(resizedImg);
	    }
	    emojiLabel.setIcon(icon);

	    emojiPanel.add(emojiLabel);

	    try {
	        t_display.setCaretPosition(t_display.getDocument().getLength());
	        t_display.insertComponent(emojiPanel); // 이모티콘 패널 추가
	        document.insertString(document.getLength(), "\n", null); // 줄바꿈 추가
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    }
	}



	private void sendImage() {
	    String filename = t_input.getText().strip();
	    if (filename.isEmpty()) return;

	    File file = new File(filename);
	    if (!file.exists()) {
	        printDisplay(">> 파일이 존재하지 않습니다: " + filename);
	        return;
	    }

	    ImageIcon icon = new ImageIcon(filename);
	    printDisplay(icon, true); // 사용자 메시지로 처리
	    t_input.setText("");
	}



    public static Color getRandomColor(String userId) {
        Random rand = new Random(userId.hashCode());
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

}
