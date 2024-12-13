import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.ArrayList;
import java.util.List;
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

public class MainScreen extends JFrame {
    private JPanel postPanel;
    private String userId;
    private Color userColor;
    private Socket socket;
    private ObjectOutputStream out;
    private Thread receiveThread;
    private String userListStr = ""; // 유저 목록 저장
    private String chatRoomListStr = ""; // 채팅방 목록 저장

    
    public MainScreen(String userId, String serverAddress, int serverPort) {
        this.userId = userId;
        this.userColor = getRandomColor(userId);

        setTitle("Hansunggram - 메인 화면");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        try {
            connectToServer(serverAddress, serverPort);
            sendUserID(); // 서버에 사용자 ID 전송
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        buildGUI();
        requestPosts(); // 초기 실행 시 게시물 요청
        setVisible(true);
    }

    private void connectToServer(String serverAddress, int serverPort) throws IOException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000); // 3초 타임아웃 설정

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        // 수신 스레드 초기화
        receiveThread = new Thread(() -> {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))) {
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

    public ObjectOutputStream getOutputStream() {
        return out;
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
        JButton postButton = createNavButton("➕", e -> new PostUploadScreen(this, userId, socket, out));

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
            case ChatMsg.MODE_REQUEST_POSTS: // 서버로부터 게시물 리스트 수신
            case ChatMsg.MODE_TX_POST:      // 새로운 게시물 수신
                addPost(inMsg.message, inMsg.image, inMsg.userID);
                break;
            case ChatMsg.MODE_TX_USER_LIST: // 유저 목록 저장
                userListStr = inMsg.message;
                System.out.println("현재 유저 목록: " + userListStr);
                break;
            case ChatMsg.MODE_CREATE_CHAT_ROOM:
            	
            	break;
            case ChatMsg.MODE_REQUEST_CHAT_ROOMS:
            	String[] rooms = inMsg.message.split("::");
                //HashSet<String> uniqueRooms = new HashSet<>(Arrays.asList(rooms)); // 중복 제거
            	List<String> roomList = new ArrayList<>();
            	for (String room : rooms) {
            	    // roomList에 아직 이 room이 없다면 추가한다.
            	    if (!roomList.contains(room)) {
            	        roomList.add(room);
            	    }
            	}
                chatRoomListStr = String.join("::", roomList);
                System.out.println("현재 채팅방 목록: " + chatRoomListStr);
            	
                break;

            default:
                System.err.println("알 수 없는 메시지 모드: " + inMsg.mode);
        }
    }
    
    public String getChatRoomList() {
        return chatRoomListStr;
    }

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

        // **맨 위에 게시물 추가**
        postPanel.add(post, 0); // index 0에 추가하여 최신 게시물이 맨 위에 표시됨
        postPanel.revalidate(); // 레이아웃 갱신
        postPanel.repaint();    // 화면 다시 그리기
    }


    public static Color getRandomColor(String userId) {
        Random rand = new Random(userId.hashCode());
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }
}