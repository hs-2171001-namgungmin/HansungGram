import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class PostUploadScreen extends JFrame {
    private JLabel imageLabel;
    private JTextField contentField;
    private JButton uploadButton, selectImageButton;
    private ImageIcon selectedImage;
    private MainScreen mainScreen;
    private Socket socket;
    private ObjectOutputStream out;
    private String userId;

    public PostUploadScreen(MainScreen mainScreen, String userId, Socket socket, ObjectOutputStream out) {
        this.mainScreen = mainScreen;
        this.userId = userId;
        this.socket = socket;
        this.out = out;

        setTitle("Hansunggram - 게시물 업로드");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        JLabel title = new JLabel("Hansunggram");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel logo = new JLabel(new ImageIcon("instagram.png"));
        panel.add(logo);
        panel.add(title);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        panel.add(imageLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        contentField = new JTextField();
        contentField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bottomPanel.add(contentField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        selectImageButton = new JButton("사진 선택");
        styleButton(selectImageButton, Color.BLUE);
        selectImageButton.addActionListener(e -> selectImage());       

        uploadButton = new JButton("게시물 업로드");
        styleButton(uploadButton, Color.GRAY);
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> uploadPost());

        buttonPanel.add(selectImageButton);
        buttonPanel.add(uploadButton);

        // 하단 네비게이션 바
        JPanel navigationPanel = new JPanel(new GridLayout(1, 3));
        navigationPanel.setBackground(new Color(230, 230, 230));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton chatButton = createNavButton("✈", e -> JOptionPane.showMessageDialog(this, "채팅 기능 준비 중입니다."));
        JButton homeButton = createNavButton("🏠", e -> {
            dispose();
            mainScreen.setVisible(true); // 홈으로 이동
        });
        JButton postButton = createNavButton("➕", null);
        postButton.setEnabled(false); // 현재 페이지는 업로드 화면

        navigationPanel.add(chatButton);
        navigationPanel.add(homeButton);
        navigationPanel.add(postButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(navigationPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, PNG, GIF 이미지 파일", "jpg", "png", "gif");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file == null || !file.exists()) {
                JOptionPane.showMessageDialog(this, "유효한 파일을 선택하세요.", "파일 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 이미지 파일을 ImageIcon으로 로드
            ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());
            Image originalImage = originalIcon.getImage();

            // JLabel의 크기에 맞게 이미지 크기 조정
            int labelWidth = imageLabel.getWidth();
            int labelHeight = imageLabel.getHeight();

            // 이미지 크기 조정
            Image scaledImage = originalImage.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);

            // 조정된 이미지를 ImageIcon으로 설정
            selectedImage = new ImageIcon(scaledImage);
            imageLabel.setIcon(selectedImage);

            // 게시물 업로드 버튼 활성화 및 색상 변경
            uploadButton.setEnabled(true);
            uploadButton.setBackground(Color.BLUE); // 버튼 색상을 파란색으로 변경
        }
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

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setPreferredSize(new Dimension(120, 40));
    }

    private void send(ChatMsg msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 전송 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void uploadPost() {
        String content = contentField.getText().trim();
        if (content.isEmpty() || selectedImage == null) {
            JOptionPane.showMessageDialog(this, "내용과 이미지를 모두 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 게시물 객체 생성 후 서버로 전송
            ChatMsg postMsg = new ChatMsg(userId, ChatMsg.MODE_TX_POST, content, selectedImage);
            out.writeObject(postMsg); // 객체 전송
            out.flush(); // 스트림 플러시

            JOptionPane.showMessageDialog(this, "게시물이 성공적으로 업로드되었습니다!");
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "업로드 중 오류 발생: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

}
