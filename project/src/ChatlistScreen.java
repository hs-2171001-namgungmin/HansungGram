import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ChatlistScreen extends JFrame {
    private JButton undo;
    private JButton plus;
    private MainScreen mainScreen;
    private String userId;
    private JPanel centerPanel;
    private Timer timer;

    public ChatlistScreen(MainScreen mainScreen, String userId) {
        this.mainScreen = mainScreen;
        this.userId = userId;
        setTitle("ChatlistScreen");

        buildGUI();
        
        // 처음 열릴 때 즉시 채팅방 목록 요청
        requestChatRoomList();
        
        // 타이머 설정 (예: 5초마다 갱신)
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
                    if (room.contains(userId) && !isChatRoomButtonExists(room)) { // 기존에 없는 버튼만 추가
                        addChatRoomButton(room);
                    }
                }
            }

            centerPanel.revalidate();
            centerPanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버와의 연결에 문제가 발생했습니다. 다시 시도해 주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

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
                    Thread.sleep(500); // 잠시 대기하여 수신 스레드가 서버 응답을 처리할 시간을 확보

                    
                    // 유저 목록 수신 후 다이얼로그 생성
                    String userListStr = mainScreen.getUserList();
                    if (userListStr == null || userListStr.isEmpty()) {
                        userListStr = "현재 접속된 사용자가 없습니다.";
                    }

                    // 다이얼로그에 유저 목록 표시 (체크박스로 표시)
                    JDialog dialog = new JDialog((Frame) null, "Select Chat", true);
                    dialog.setLayout(new GridLayout(0, 1));

                    String[] users = userListStr.split(", ");
                    JCheckBox[] userCheckBoxes = users.length > 1 ? new JCheckBox[users.length - 1] : new JCheckBox[0];
                    int index = 0;
                    for (int i = 0; i < users.length; i++) {
                        if (!users[i].equals(userId)) { // 자기 자신 제외
                            userCheckBoxes[index] = new JCheckBox(users[i]);
                            dialog.add(userCheckBoxes[index]);
                            index++;
                        }
                    }

                    JButton createChatButton = new JButton("Create Chat Room");
                    createChatButton.setContentAreaFilled(false); // 버튼 배경색 투명
                    createChatButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            StringBuilder selectedUsers = new StringBuilder(userId);
                            for (JCheckBox checkBox : userCheckBoxes) {
                                if (checkBox != null && checkBox.isSelected()) {
                                    selectedUsers.append(", ").append(checkBox.getText());
                                }
                            }
                            if (selectedUsers.length() > 0) {
                                addChatRoomButton(selectedUsers.toString());
                                
                                try {
                                    ChatMsg createChatRoomMsg = new ChatMsg(userId, ChatMsg.MODE_CREATE_CHAT_ROOM, selectedUsers.toString());
                                    mainScreen.getOutputStream().writeObject(createChatRoomMsg);
                                    mainScreen.getOutputStream().flush();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            dialog.dispose();
                        }
                    });

                    dialog.add(createChatButton);
                    dialog.setSize(300, 300);
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
        centerPanel.setLayout(new GridLayout(0, 1));
        centerPanel.setBackground(Color.white);
        return centerPanel;
    }

    private void addChatRoomButton(String chatRoomName) {
        JButton chatRoomButton = new JButton(chatRoomName);
        chatRoomButton.setBackground(Color.LIGHT_GRAY);
        chatRoomButton.setContentAreaFilled(false); // 버튼 배경색 투명
        chatRoomButton.setFocusPainted(false);
        chatRoomButton.setBorderPainted(true);

        // 채팅방 버튼 클릭 이벤트
        chatRoomButton.addActionListener(e -> {
            // ChatScreen으로 이동
            new ChatScreen(chatRoomName, userId); // 새로운 ChatScreen 생성
        });

        centerPanel.add(chatRoomButton);
        centerPanel.revalidate();
        centerPanel.repaint();

        
        /*
        try {
            ChatMsg createChatRoomMsg = new ChatMsg(userId, ChatMsg.MODE_CREATE_CHAT_ROOM, chatRoomName);
            mainScreen.getOutputStream().writeObject(createChatRoomMsg);
            mainScreen.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }
}
