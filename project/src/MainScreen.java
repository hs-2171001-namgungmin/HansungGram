import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

public class MainScreen extends JFrame {
    private JPanel postPanel;
    private String userId;
    private Color userColor;
    private Socket socket;
    private ObjectOutputStream out;
    private Thread receiveThread;

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
        setVisible(true);
    }

    private void connectToServer(String serverAddress, int serverPort) throws IOException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000); // 3초 타임아웃 설정

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        // 수신 스레드 초기화
        receiveThread = new Thread(new Runnable() {
            private ObjectInputStream in;

            private void receiveMessage() {
                try {
                    ChatMsg inMsg = (ChatMsg) in.readObject();
                    if (inMsg == null) {
                        disconnect();
                        System.out.println("서버 연결 끊김");
                        return;
                    }
                    switch (inMsg.mode) {
                        case ChatMsg.MODE_TX_STRING:
                            System.out.println(inMsg.userID + ": " + inMsg.message);
                            break;

                        case ChatMsg.MODE_TX_IMAGE:
                            System.out.println(inMsg.userID + ": " + inMsg.message);
                            // 이미지 처리 로직 추가 가능
                            break;

                        case ChatMsg.MODE_TX_POST:
                            addPost(inMsg.message, inMsg.image, inMsg.userID); // 게시물 추가
                            break;
                    }
                } catch (IOException e) {
                    System.err.println("클라이언트 수신 오류: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    System.out.println("잘못된 객체가 전달되었습니다.");
                }
            }

            @Override
            public void run() {
                try {
                    in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    System.err.println("입력 스트림이 열리지 않음: " + e.getMessage());
                }
                while (!Thread.currentThread().isInterrupted()) {
                    receiveMessage();
                }
            }
        });
        receiveThread.start();

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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 하단 패널 높이 증가
        panel.setBackground(new Color(230, 230, 230)); // 연한 회색 배경 설정

        JButton chatButton = new JButton("✈");
        JButton homeButton = new JButton("🏠");
        JButton postButton = new JButton("➕");

        Font buttonFont = new Font("SansSerif", Font.BOLD, 18);
        chatButton.setFont(buttonFont);
        homeButton.setFont(buttonFont);
        postButton.setFont(buttonFont);

        chatButton.setFocusPainted(false);
        homeButton.setFocusPainted(false);
        postButton.setFocusPainted(false);

        chatButton.setBackground(new Color(230, 230, 230));
        homeButton.setBackground(new Color(230, 230, 230));
        postButton.setBackground(new Color(230, 230, 230));
        chatButton.setBorder(BorderFactory.createEmptyBorder());
        homeButton.setBorder(BorderFactory.createEmptyBorder());
        postButton.setBorder(BorderFactory.createEmptyBorder());

        homeButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "홈으로 이동합니다."));
        postButton.addActionListener(e -> new PostUploadScreen(this, userId, socket, out));
        chatButton.addActionListener(e -> new ChatlistScreen(this, null, userId));

        panel.add(chatButton);
        panel.add(homeButton);
        panel.add(postButton);

        return panel;
    }

    public void addPost(String content, ImageIcon image, String userId) {
        JPanel post = new JPanel(new BorderLayout());
        post.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        post.setBackground(Color.WHITE);

        // 사용자 정보 패널
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(Color.WHITE);

        // 프로필 원형 라벨
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

        // 이미지 및 내용
        JLabel imageLabel = new JLabel(image);
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // 게시물 구성
        post.add(userInfoPanel, BorderLayout.NORTH);
        post.add(imageLabel, BorderLayout.CENTER);
        post.add(contentLabel, BorderLayout.SOUTH);

        postPanel.add(post, 0);
        postPanel.revalidate();
        postPanel.repaint();
    }

    private Color getRandomColor(String userId) {
        Random rand = new Random(userId.hashCode()); // 사용자 ID 기반으로 랜덤 생성
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.interrupt();
            }
            System.out.println("연결이 성공적으로 종료되었습니다.");
        } catch (IOException e) {
            System.err.println("연결 해제 오류: " + e.getMessage());
        }
    }

}
