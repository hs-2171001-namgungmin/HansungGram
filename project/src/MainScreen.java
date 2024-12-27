import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MainScreen extends JFrame {
    private JPanel postPanel;
    private String userId;
    private Color userColor;
    private Socket socket;
    private ObjectOutputStream out;
    private Thread receiveThread;
    private String userListStr = ""; // 유저 목록 저장
    private String chatRoomListStr = ""; // 채팅방 목록 저장
    private Map<String, ChatScreen> chatScreens = new HashMap<>(); // 채팅방 이름별 ChatScreen 저장
    private ObjectInputStream in;

    
    public MainScreen(String userId, Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.userId = userId;
        this.socket = socket;
        this.out = out;
        this.in = in; // ObjectInputStream 저장

        setTitle("Hansunggram - 메인 화면");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 수신 스레드 시작
        startReceivingMessages(in);

        buildGUI();
        requestPosts(); // 게시물 요청
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                sendLogoutMessage(); // 로그아웃 메시지 전송
                System.exit(0);      // 프로그램 종료
            }
        });

    }
    private void sendLogoutMessage() {
        try {
            // 로그아웃 메시지 생성 및 전송
            ChatMsg logoutMsg = new ChatMsg(userId, ChatMsg.MODE_LOGOUT);
            out.writeObject(logoutMsg);
            out.flush();
            System.out.println("로그아웃 메시지 전송 완료.");
        } catch (IOException e) {
            System.err.println("로그아웃 메시지 전송 오류: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }
    public ObjectInputStream getInputStream() {
        return in;
    }

    private void startReceivingMessages(ObjectInputStream in) {
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    ChatMsg inMsg = (ChatMsg) in.readObject();
                    if (inMsg == null) {
                        System.out.println("서버로부터 null 메시지 수신. 연결 종료 처리.");
                        break;
                    }
                    processIncomingMessage(inMsg);
                }
            } catch (IOException e) {
                if (!e.getMessage().contains("Socket closed")) {
                    System.err.println("메시지 수신 중 오류 발생: " + e.getMessage());
                } else {
                    System.out.println("서버와의 연결이 종료되었습니다.");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("수신 데이터 타입 오류: " + e.getMessage());
            } finally {
                try {
                    System.out.println("서버와의 연결이 종료되었습니다.");
                    in.close();
                    socket.close();
                    System.exit(0); // 클라이언트 종료
                } catch (IOException ex) {
                    System.err.println("스트림 종료 중 오류 발생: " + ex.getMessage());
                }
            }
        });
        receiveThread.start();
    }


    public void addChatScreen(String chatRoomName, ChatScreen chatScreen) {
        chatScreens.put(chatRoomName, chatScreen);
    }

    public ChatScreen getChatScreen(String chatRoomName) {
        return chatScreens.get(chatRoomName);
    }

    private void connectToServer(String serverAddress, int serverPort) throws IOException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000); // 3초 타임아웃 설정

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.flush();
        
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        // 수신 스레드 초기화
        receiveThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ChatMsg inMsg = (ChatMsg) in.readObject();
                    if (inMsg != null) {
                        processIncomingMessage(inMsg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("메시지 수신 중 오류 발생: " + e.getMessage());
            }
        });
        receiveThread.start();
    }
    public String getUserList() {
        return userListStr;
    }

    private void sendUserID() throws IOException {
        ChatMsg loginMsg = new ChatMsg(userId, ChatMsg.MODE_LOGIN);
        out.writeObject(loginMsg);
        out.flush();
    }

    private void buildGUI() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        JLabel appName = new JLabel("Hansunggram");
        appName.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel logo = new JLabel(new ImageIcon("instagram.png"));
        panel.add(logo);
        panel.add(appName);
        return panel;
    }

    private JScrollPane createCenterPanel() {
        postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
        postPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(postPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBackground(new Color(230, 230, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton chatButton = createNavButton("✈", e -> new ChatlistScreen(this, userId));
        JButton homeButton = createNavButton("🏠", e -> JOptionPane.showMessageDialog(this, "홈 화면입니다."));
        JButton postButton = createNavButton("➕", e -> 
        new PostUploadScreen(this, userId)
    );
        panel.add(chatButton);
        panel.add(homeButton);
        panel.add(postButton);
        return panel;
    }

    private JButton createNavButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(230, 230, 230));
        button.setBorder(BorderFactory.createEmptyBorder());
        if (action != null) {
            button.addActionListener(action);
        }
        return button;
    }

    private void requestPosts() {
        try {
            ChatMsg requestMsg = new ChatMsg(userId, ChatMsg.MODE_REQUEST_POSTS);
            out.writeObject(requestMsg);
            out.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "게시물 요청 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processIncomingMessage(ChatMsg inMsg) {
        switch (inMsg.mode) {
        case ChatMsg.MODE_LOGOUT:
            SwingUtilities.invokeLater(() -> {
                for (ChatScreen chatScreen : chatScreens.values()) {
                    if (chatScreen.isVisible()) {
                        chatScreen.displayMessage("Server", inMsg.message); // 채팅 화면에 메시지 추가
                    }
                }
            });
            break;
            case ChatMsg.MODE_REQUEST_POSTS: // 서버로부터 게시물 리스트 수신
            case ChatMsg.MODE_TX_POST:      // 새로운 게시물 수신
                addPost(inMsg.message, inMsg.image, inMsg.userID);
                break;
            case ChatMsg.MODE_TX_USER_LIST: //유저 목록 저장
                userListStr = inMsg.message;
                System.out.println("현재 유저 목록: " + userListStr);
                break;
            case ChatMsg.MODE_CREATE_CHAT_ROOM:
            	
            	break;
            case ChatMsg.MODE_TX_STRING:
                String[] parts = inMsg.message.split("::", 2);
                if (parts.length == 2) {
                    String[] users = parts[0].split(", ");
                    Arrays.sort(users);
                    String chatRoomName = String.join(", ", users);
                    String messageContent = parts[1];

                    SwingUtilities.invokeLater(() -> {
                        ChatScreen chatScreen = chatScreens.get(chatRoomName);
                        if (chatScreen == null) {
                            chatScreen = new ChatScreen(chatRoomName, userId, out, in);
                            chatScreens.put(chatRoomName, chatScreen);
                        }
                        chatScreen.displayMessage(inMsg.userID, messageContent);
                    });
                }
                break;
            case ChatMsg.MODE_REQUEST_CHAT_HISTORY: // 채팅 기록 수신 처리
            	 String[] msgParts = inMsg.message.split("::", 2);
            	    if (msgParts.length == 2) {
            	        String chatRoomName = msgParts[0];
            	        String messageContent = msgParts[1];
            	        
            	        // ChatScreen 인스턴스 가져오기
            	        SwingUtilities.invokeLater(() -> {
            	            ChatScreen chatScreen = chatScreens.get(chatRoomName);
            	            if (chatScreen != null) {
            	                chatScreen.displayMessage(inMsg.userID, messageContent);
            	            }
            	        });
            	    }
            	    break;
            case ChatMsg.MODE_TX_IMAGE: // 이미지 메시지 수신 시 처리
                SwingUtilities.invokeLater(() -> {
                    String[] parts1 = (inMsg.message != null) ? inMsg.message.split("::") : new String[] {"Unknown Room", ""};
                    String chatRoomName = parts1[0]; // 채팅방 이름
                    ChatScreen chatScreen = chatScreens.get(chatRoomName);

                    // 채팅방이 없으면 새로 생성
                    if (chatScreen == null) {
                        chatScreen = new ChatScreen(chatRoomName, userId, out, in);
                        chatScreens.put(chatRoomName, chatScreen);
                    }

                    // 채팅 화면에 이미지 표시
                    chatScreen.printDisplay(inMsg.image, inMsg.userID, inMsg.userID.equals(userId));
                });
                break;

            case ChatMsg.MODE_REQUEST_CHAT_ROOMS:
                if (inMsg.message != null && !inMsg.message.isEmpty()) {
                	chatRoomListStr = inMsg.message;
                	// ChatlistScreen에 업데이트 요청
                    SwingUtilities.invokeLater(() -> {
                        // 열려있는 모든 Frame 확인
                        Frame[] frames = JFrame.getFrames();
                        for (Frame frame : frames) {
                            if (frame instanceof ChatlistScreen && frame.isVisible()) {
                                // updateChatRoomList() 호출
                                ((ChatlistScreen) frame).updateChatRoomList(inMsg.message);
                            }
                        }
                    });
                    System.out.println("현재 채팅방 목록: " + chatRoomListStr);
                }
                break;
            case ChatMsg.MODE_LEAVE_CHAT_ROOM:
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, inMsg.userID + "님이 채팅방을 나갔습니다.");
                });
                break;
            case ChatMsg.MODE_TX_FILE:
                String fileName = inMsg.message; //파일 이름만 받음

                SwingUtilities.invokeLater(() -> {
                    // 이미 존재하는 ChatScreen 인스턴스를 가져옴
                    for (ChatScreen chatScreen : chatScreens.values()) {
                        if (chatScreen.isVisible()) { //현재 열려있는 채팅 화면에 파일 표시
                            chatScreen.displayFileMessage(inMsg.userID, fileName);
                            break;
                        }
                    }
                });
                break;







            default:
                System.err.println("알 수 없는 메시지 모드: " + inMsg.mode);
        }
    }
    
    public String getChatRoomList() {
        return chatRoomListStr;
    }

    // java Grapics2D를 사용한 도형 그리기 (자료참고)
    public void addPost(String content, ImageIcon image, String userId) { 
        JPanel post = new JPanel(new BorderLayout());
        post.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        post.setBackground(Color.WHITE);

        // 사용자 정보 패널
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(Color.WHITE);

        JLabel profilePic = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getRandomColor(userId)); // 사용자 고유 색상
                g2d.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        profilePic.setPreferredSize(new Dimension(40, 40));

        JLabel userLabel = new JLabel(userId);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        userInfoPanel.add(profilePic);
        userInfoPanel.add(userLabel);

        // 게시물 내용 및 이미지
        JLabel imageLabel = new JLabel(image);
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        post.add(userInfoPanel, BorderLayout.NORTH);
        post.add(imageLabel, BorderLayout.CENTER);
        post.add(contentLabel, BorderLayout.SOUTH);

        // 맨 위에 게시물 추가
        postPanel.add(post, 0); // index 0에 추가하여 최신 게시물이 맨 위에 표시됨
        postPanel.revalidate(); // 레이아웃 갱신
        postPanel.repaint();    // 화면 다시 그리기
    }


    public static Color getRandomColor(String userId) {
        Random rand = new Random(userId.hashCode());
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }
}