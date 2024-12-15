import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class ChatlistScreen extends JFrame {
	private JButton undo;
	private JButton plus;
	private MainScreen mainScreen;
	private String userId;
	private JPanel centerPanel;
	private Timer timer; //주기적으로 채팅방 목록을 요청하는 타이머

	public ChatlistScreen(MainScreen mainScreen, String userId) {
		this.mainScreen = mainScreen;
		this.userId = userId;
		setTitle("ChatlistScreen");

		buildGUI();

		// 처음 열릴 때 즉시 채팅방 목록 요청
		requestChatRoomList();

		// 5초마다 채팅방 목록 요청
		timer = new Timer(5000, e -> requestChatRoomList());
		timer.start();

		// 창이 닫힐 때 타이머 종료
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				timer.stop();
			}
		});

		setSize(400, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	// 서버에 채팅방 목록 요청
	private void requestChatRoomList() {
		try {
			// 서버에 채팅방 목록 요청 전송
			ChatMsg requestChatRoomsMsg = new ChatMsg(userId, ChatMsg.MODE_REQUEST_CHAT_ROOMS);
			mainScreen.getOutputStream().writeObject(requestChatRoomsMsg);
			mainScreen.getOutputStream().flush();

			// 서버로부터 받은 채팅방 목록 가져오기
			String chatRoomList = mainScreen.getChatRoomList();
			if (chatRoomList != null && !chatRoomList.isEmpty()) {
				String[] chatRooms = chatRoomList.split("::");
				for (String room : chatRooms) {
					// 이미 존재하는 버튼인지 확인 후 추가
					if (room.contains(userId) && !isChatRoomButtonExists(room)) { //기존에 없는 버튼만 추가
						addChatRoomButton(room);
					}
				}
			}

			centerPanel.revalidate(); //레이아웃 갱신
			centerPanel.repaint();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "서버와의 연결에 문제가 발생했습니다. 다시 시도해 주세요.", "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

	// 채팅방 버튼이 이미 존재하는지 확인
	private boolean isChatRoomButtonExists(String chatRoomName) {
		for (int i = 0; i < centerPanel.getComponentCount(); i++) {
			if (centerPanel.getComponent(i) instanceof JButton) {
				JButton button = (JButton) centerPanel.getComponent(i);
				if (button.getText().equals(chatRoomName)) {
					return true;
				}
			}
		}
		return false;
	}

	private void buildGUI() {
		add(createTopPanel(), BorderLayout.NORTH);
		add(createCenterPanel(), BorderLayout.CENTER);
	}

	private JPanel createTopPanel() {
		JPanel p = new JPanel(new GridLayout(2, 0));
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

		JLabel user_name = new JLabel(userId);

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
				try {
					// 서버에 유저 목록 요청 전송
					ChatMsg requestUserListMsg = new ChatMsg(userId, ChatMsg.MODE_TX_USER_LIST);
					mainScreen.getOutputStream().writeObject(requestUserListMsg);
					mainScreen.getOutputStream().flush();

					// 서버 응답 대기 (MainScreen의 수신 스레드에서 userListStr이 업데이트될 때까지 잠시 대기)
					Thread.sleep(500); //잠시 대기하여 수신 스레드가 서버 응답을 처리할 시간을 확보

					// 유저 목록 수신 후 다이얼로그 생성
					String userListStr = mainScreen.getUserList();
					if (userListStr == null || userListStr.isEmpty()) {
						userListStr = "현재 접속된 사용자가 없습니다.";
					}

					// 다이얼로그에 유저 목록 표시 (체크박스로 표시)
					JDialog dialog = new JDialog((Frame) null, "채팅방 생성", true);
					dialog.setLayout(new BorderLayout()); //전체 레이아웃을 BorderLayout으로 설정

					// 체크박스 리스트 패널
					JPanel userListPanel = new JPanel();
					userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS)); //수직 정렬
					userListPanel.setBackground(Color.WHITE);

					String[] users = userListStr.split(", ");
					JCheckBox[] userCheckBoxes = new JCheckBox[users.length];
					for (int i = 0; i < users.length; i++) {
						if (!users[i].equals(userId)) { //자기 자신 제외
							JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //수평 정렬
							checkBoxPanel.setBackground(Color.WHITE);

							JCheckBox checkBox = new JCheckBox();
							JLabel userLabel = new JLabel(users[i]);
							userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

							checkBoxPanel.add(checkBox);
							checkBoxPanel.add(userLabel);

							userListPanel.add(checkBoxPanel);
							userCheckBoxes[i] = checkBox;
						}
					}

					// 하단 버튼 패널
					JButton createChatButton = new JButton("새로운 채팅방 만들기");
					createChatButton.setFont(new Font("SansSerif", Font.BOLD, 14));
					createChatButton.setBackground(new Color(50, 50, 50));
					createChatButton.setForeground(Color.WHITE);
					createChatButton.setFocusPainted(false);
					createChatButton.setBorderPainted(false);
					createChatButton.setPreferredSize(new Dimension(0, 40)); //버튼 높이 고정
					createChatButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							StringBuilder selectedUsers = new StringBuilder(userId);
							for (int i = 0; i < userCheckBoxes.length; i++) {
								if (userCheckBoxes[i] != null && userCheckBoxes[i].isSelected()) {
									selectedUsers.append(", ").append(users[i]);
								}
							}
							if (selectedUsers.length() > 0) {
								addChatRoomButton(selectedUsers.toString());
							}
							dialog.dispose();
						}

					});

					// 다이얼로그에 패널 추가
					dialog.add(userListPanel, BorderLayout.CENTER);
					dialog.add(createChatButton, BorderLayout.SOUTH);

					dialog.setSize(250, 300);
					dialog.setLocationRelativeTo(null); //화면 중앙에 표시
					dialog.setVisible(true);

				} catch (IOException | InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		});

		p2.add(message, BorderLayout.WEST);
		p2.add(plus, BorderLayout.EAST);

		p.add(p1);
		p.add(p2);

		return p;
	}

	private JPanel createCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(Color.white);
		return centerPanel;
	}

	// 새로운 채팅방 버튼 추가
	private void addChatRoomButton(String chatRoomName) {
		JButton chatRoomButton = new JButton(chatRoomName);
		chatRoomButton.setBackground(Color.LIGHT_GRAY);
		chatRoomButton.setContentAreaFilled(false); //버튼 배경색 투명
		chatRoomButton.setFocusPainted(false);
		chatRoomButton.setBorderPainted(true);

		// 버튼 텍스트 왼쪽 정렬
		chatRoomButton.setHorizontalAlignment(SwingConstants.LEFT);

		// 버튼 크기 설정 (centerPanel의 가로폭, centerPanel의 세로 1/7 크기)
		chatRoomButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, centerPanel.getHeight() / 7));

		chatRoomButton.addActionListener(e -> {
			// ChatScreen으로 이동
			new ChatScreen(chatRoomName, userId); //새로운 ChatScreen 생성
		});

		centerPanel.add(chatRoomButton);
		centerPanel.revalidate(); //새 버튼 추가 후 레이아웃 갱신
		centerPanel.repaint(); //화면 다시 그리기

	}
}
