import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
	private JTextPane t_display;
	private JButton b_image, b_emoji, b_file;
	private DefaultStyledDocument document;
	private String userId;
	private String lastSender = ""; // 마지막 메시지 보낸 사용자
	private ObjectOutputStream out; // 서버로 메시지 전송에 사용
	private String chatRoomName;
	private ObjectInputStream in; // 서버로부터 데이터를 읽는 입력 스트림


	public ChatScreen(String chatRoomName, String userId, ObjectOutputStream out, ObjectInputStream in) {
		super("Chat with " + chatRoomName);
		this.userId = userId;
		this.chatRoomName = chatRoomName;

		if (out == null) {
			throw new IllegalArgumentException("ObjectOutputStream cannot be null");
		}
		this.out = out;
		this.in = in; // ObjectInputStream 추가
		buildGUI();
		requestChatHistory(); // 채팅방 진입 시 채팅 기록 요청
		setSize(400, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setVisible(true);
	}

	public String getChatRoomName() {
		return chatRoomName;
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

	    // 뒤로 가기 버튼
	    undo = new JButton("◀");
	    undo.setBackground(Color.white);
	    undo.addActionListener(e -> dispose());
	    undo.setFocusPainted(false);
	    undo.setBorderPainted(false);

	    // 사용자 이름 표시
	    JLabel userName = new JLabel(userId);

	    // 나가기 버튼 추가
	    JButton leaveButton = new JButton("나가기");
	    leaveButton.setBackground(Color.white);
	    leaveButton.setFocusPainted(false);
	    leaveButton.setBorderPainted(false);
	    leaveButton.addActionListener(e -> {
	        try {
	            // 서버에 채팅방 나가기 메시지 전송
	            ChatMsg leaveMsg = new ChatMsg(userId, ChatMsg.MODE_LEAVE_CHAT_ROOM, chatRoomName);
	            out.writeObject(leaveMsg);
	            out.flush();

	            // 사용자에게 알림 후 화면 닫기
	            JOptionPane.showMessageDialog(this, "채팅방에서 나갔습니다.");
	            dispose(); // 채팅 화면 닫기
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(this, "나가기 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
	        }
	    });


	    // p1 패널에 컴포넌트 추가
	    p1.add(undo);
	    p1.add(userName);
	    p1.add(leaveButton); // 나가기 버튼 추가

	    // 메시지 레이블과 구분선 추가
	    JPanel p2 = new JPanel(new BorderLayout());
	    p2.setBackground(Color.WHITE);

	    JLabel message = new JLabel("    메시지");
	    message.setFont(new Font("", Font.PLAIN, 10));

	    p2.add(message, BorderLayout.WEST);

	    JSeparator separator = new JSeparator();
	    separator.setForeground(Color.GRAY);

	    // 상위 패널에 p1과 p2 추가
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

		t_input = new JTextField(28);
		t_input.setBorder(null); // 테두리 삭제
		t_input.setBackground(Color.LIGHT_GRAY);
		// 메시지 전송 이벤트 추가
		t_input.addActionListener(e -> {
			String message = t_input.getText();
			if (!message.isEmpty()) {
				sendMessageToServer(message); // 서버에 메시지 전송
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
						try {
							ChatMsg emojiMsg = new ChatMsg(userId, ChatMsg.MODE_TX_IMAGE, chatRoomName + "::emoji",
									resizedIcon);
							out.writeObject(emojiMsg);
							out.flush();
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(null, "이미지 전송 실패: " + ex.getMessage(), "오류",
									JOptionPane.ERROR_MESSAGE);
						}
					});

					dialog.add(emoButton);
				}

				dialog.setSize(250, 250);
				dialog.setVisible(true);
			}
		});

		ImageIcon image = new ImageIcon("photo.png");
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
				FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF & PNG Images", // 파일 이름에 창에 출력될
																										// 문자열
						"jpg", "gif", "png"); // 파일 필터로 사용되는 확장자

				chooser.setFileFilter(filter);

				int ret = chooser.showOpenDialog(ChatScreen.this);
				if (ret != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(ChatScreen.this, "파일을 선택하지 않았습니다.");
					return;
				}

				t_input.setText(chooser.getSelectedFile().getAbsolutePath());
				sendImage();

			}

		});
		
		ImageIcon file = new ImageIcon("File plus.png");
		Image img3 = file.getImage();
		Image newImg3 = img3.getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH);
		
		b_file = new JButton(new ImageIcon(newImg3));
		b_file.setPreferredSize(new Dimension(28, 28));
		b_file.setBackground(Color.white);
		b_file.setFocusPainted(false);
		b_file.setBorderPainted(false);
		b_file.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
		        int returnVal = fileChooser.showOpenDialog(ChatScreen.this);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            long fileSize = selectedFile.length();
		            if (fileSize > 10 * 1024 * 1024) { // 10MB 제한
		                JOptionPane.showMessageDialog(ChatScreen.this, "파일 크기가 너무 큽니다. (10MB 이하만 전송 가능)", "오류", JOptionPane.ERROR_MESSAGE);
		                return;
		            }
		            try {
		                // 서버에 파일 전송 메타 데이터 전송
		                ChatMsg fileMsg = new ChatMsg(userId, ChatMsg.MODE_TX_FILE, selectedFile.getName(), fileSize);
		                out.writeObject(fileMsg);
		                out.flush();

		                // 파일 데이터를 서버로 스트리밍
		                byte[] buffer = new byte[8192];
		                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile))) {
		                    int bytesRead;
		                    while ((bytesRead = bis.read(buffer)) != -1) {
		                        out.write(buffer, 0, bytesRead);
		                    }
		                }
		                out.flush();
		                JOptionPane.showMessageDialog(ChatScreen.this, "파일 전송 성공!");
		            } catch (IOException ex) {
		                JOptionPane.showMessageDialog(ChatScreen.this, "파일 전송 중 오류 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		            }
		        }
				
			}
			
		});
		
		p.add(t_input);
		p.add(b_emoji);
		p.add(b_image);
		p.add(b_file);
		return p;
	}
	
	public void displayFileMessage(String senderId, String fileName) {
	    boolean isUser = senderId.equals(userId);

	    JButton fileButton = new JButton(fileName);
	    fileButton.setBackground(Color.WHITE);
	    fileButton.setFocusPainted(false);
	    fileButton.setBorderPainted(true);

	    fileButton.addActionListener(e -> {
	        JFileChooser chooser = new JFileChooser();
	        chooser.setSelectedFile(new File(fileName));
	        int option = chooser.showSaveDialog(ChatScreen.this);
	        if (option == JFileChooser.APPROVE_OPTION) {
	            try {
	                // 서버에 파일 요청 메시지 전송
	                out.writeObject(new ChatMsg(userId, ChatMsg.MODE_REQUEST_FILE, fileName));
	                out.flush();

	                File saveFile = chooser.getSelectedFile();
	                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveFile))) {
	                    byte[] buffer = new byte[8192];
	                    long fileSize = in.readLong(); // 서버에서 파일 크기를 읽음
	                    long totalBytesRead = 0;
	                    int bytesRead;

	                    // 파일 크기만큼 읽기
	                    while (totalBytesRead < fileSize && 
	                          (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
	                        bos.write(buffer, 0, bytesRead);
	                        totalBytesRead += bytesRead;
	                    }
	                    bos.flush();
	                    JOptionPane.showMessageDialog(this, "파일 다운로드 완료!");
	                }
	            } catch (IOException ex) {
	                JOptionPane.showMessageDialog(this, "파일 다운로드 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    });

	    displayMessageWithProfile(senderId, fileButton, isUser);
	}




	// 서버로 메시지 전송 메서드
	private void sendMessageToServer(String message) {
		try {
			// 보낸 사람 ID를 메시지에 포함
			String fullMessage = chatRoomName + "::" + userId + "::" + message;
			ChatMsg chatMsg = new ChatMsg(userId, ChatMsg.MODE_TX_STRING, fullMessage);
			out.writeObject(chatMsg);
			out.flush();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "메시지 전송 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void displayMessageWithProfile(String senderId, JComponent content, boolean isUser) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setOpaque(false);

		JPanel profileAndBubblePanel = new JPanel(new BorderLayout());
		profileAndBubblePanel.setOpaque(false);
		profileAndBubblePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 여백 추가

		// 프로필과 사용자 아이디는 상대방 메시지이거나, 내 메시지가 연속되지 않은 경우만 표시
		if (!senderId.equals(lastSender)) {
			if (!isUser) { //상대방이 보낸 경우
				JLabel profileLabel = new JLabel() {
					@Override
					protected void paintComponent(Graphics g) {
						super.paintComponent(g);
						Graphics2D g2d = (Graphics2D) g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setColor(getRandomColor(senderId));
						g2d.fillOval(0, 0, getWidth(), getHeight());
					}
				};
				profileLabel.setPreferredSize(new Dimension(30, 30));

				JLabel userIdLabel = new JLabel(senderId);
				userIdLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
				userIdLabel.setForeground(Color.GRAY);

				JPanel profilePanel = new JPanel(new BorderLayout());
				profilePanel.setOpaque(false);
				profilePanel.add(profileLabel, BorderLayout.WEST);
				profilePanel.add(Box.createHorizontalStrut(5), BorderLayout.CENTER); //여백 추가
				profilePanel.add(userIdLabel, BorderLayout.EAST);

				profileAndBubblePanel.add(profilePanel, BorderLayout.WEST);
			}
		}

		// 내용 추가
		profileAndBubblePanel.add(content, BorderLayout.SOUTH);
		messagePanel.add(profileAndBubblePanel, isUser ? BorderLayout.EAST : BorderLayout.WEST);

		try {
			int len = document.getLength();
			document.insertString(len, "\n", null);
			t_display.setCaretPosition(len);
			t_display.insertComponent(messagePanel);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		// 마지막 보낸 사용자 업데이트
		lastSender = senderId;
	}

	protected void displayMessage(String senderId, String message) {
		boolean isUser = senderId.equals(userId); // 보낸 사람 ID가 나의 ID와 같으면 true

		// 메시지에서 '::' 제거
		if (message.contains("::")) {
			message = message.split("::", 2)[1];
		}

		// 말풍선 설정
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

		bubbleLabel.setText("<html><p style='padding: 10px; white-space: normal;'>" + message + "</p></html>");
		bubbleLabel.setForeground(Color.WHITE);
		bubbleLabel.setOpaque(false);

		displayMessageWithProfile(senderId, bubbleLabel, isUser);
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

	protected void printDisplay(ImageIcon icon, String senderId, boolean isUser) {
		JLabel emojiLabel = new JLabel(icon);

		// 이미지 크기 조정
		if (icon.getIconWidth() > 400) {
			Image img = icon.getImage();
			Image resizedImg = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
			icon = new ImageIcon(resizedImg);
		}
		emojiLabel.setIcon(icon);

		displayMessageWithProfile(senderId, emojiLabel, isUser);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ChatScreen other = (ChatScreen) obj;
		return chatRoomName.equals(other.chatRoomName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chatRoomName);
	}

	private void requestChatHistory() {
		try {
			ChatMsg requestHistoryMsg = new ChatMsg(userId, ChatMsg.MODE_REQUEST_CHAT_HISTORY, chatRoomName);
			out.writeObject(requestHistoryMsg);
			out.flush();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "채팅 기록 요청 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void sendImage() {
		String filename = t_input.getText().strip();
		if (filename.isEmpty())
			return;

		File file = new File(filename);
		if (!file.exists()) {
			printDisplay(">> 파일이 존재하지 않습니다: " + filename);
			return;
		}

		// ImageIcon 객체 생성
		ImageIcon icon = new ImageIcon(filename);

		try {
			// 서버로 이미지 전송
			ChatMsg imageMsg = new ChatMsg(userId, ChatMsg.MODE_TX_IMAGE, chatRoomName + "::image", icon);
			out.writeObject(imageMsg);
			out.flush();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "이미지 전송 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
		t_input.setText("");
	}

	public static Color getRandomColor(String userId) {
		Random rand = new Random(userId.hashCode());
		return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
	}

}