
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MainScreen extends JFrame {
    private JPanel postPanel;
    private String userId;
    private Color userColor;

    public MainScreen(String userId) {
        this.userId = userId;
        this.userColor = getRandomColor();
        setTitle("Hansunggram - 메인 화면");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        buildGUI();

        setVisible(true);
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
        JLabel logo = new JLabel(new ImageIcon("instagram.png")); // 로고 추가
        panel.add(logo);
        panel.add(appName);
        return panel;
    }

    private JScrollPane createCenterPanel() {
        postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS)); // 세로로 배치
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
        postButton.addActionListener(e -> new PostUploadScreen(this));
        chatButton.addActionListener(e -> new ChatlistScreen(this, userId));

        panel.add(chatButton);
        panel.add(homeButton);
        panel.add(postButton);

        return panel;
    }

    public void addPost(String content, ImageIcon image) {
        if (image == null || image.getIconWidth() <= 0 || image.getIconHeight() <= 0) {
            JOptionPane.showMessageDialog(this, "유효하지 않은 이미지를 추가하려고 시도했습니다.", "이미지 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 게시물 패널
        JPanel post = new JPanel(new BorderLayout());
        post.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        post.setBackground(Color.WHITE);

        // 사용자 정보 패널 (프로필 원형 + 이름)
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(Color.WHITE);

        // 프로필 원형
        JLabel profilePic = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(userColor); // 고유 색상
                g2d.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        profilePic.setPreferredSize(new Dimension(40, 40));

        JLabel userLabel = new JLabel(userId);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        userInfoPanel.add(profilePic);
        userInfoPanel.add(userLabel);

        // 이미지 패널
        JLabel imageLabel = new JLabel(new ImageIcon(image.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH)));

        // 내용 패널
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // 게시물 구성
        post.add(userInfoPanel, BorderLayout.NORTH);
        post.add(imageLabel, BorderLayout.CENTER);
        post.add(contentLabel, BorderLayout.SOUTH);

        // 게시물을 맨 위에 추가
        postPanel.add(post, 0);
        postPanel.revalidate();
        postPanel.repaint();
    }

    private Color getRandomColor() {
        Random rand = new Random(userId.hashCode());
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }
}
