import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class PostUploadScreen extends JFrame {
    private JLabel imageLabel;
    private JTextField contentField;
    private JButton uploadButton;
    private ImageIcon selectedImage;
    private MainScreen mainScreen;

    public PostUploadScreen(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
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
        JLabel logo = new JLabel(new ImageIcon("instagram.png")); // 로고 추가
        panel.add(logo);
        panel.add(title);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 이미지 미리보기 크기 확대
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setPreferredSize(new Dimension(300, 300)); // 크기 확대
        panel.add(imageLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);

        // 텍스트 필드 줄이기
        contentField = new JTextField();
        contentField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentField.setPreferredSize(new Dimension(400, 30)); // 높이 줄임
        contentField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        bottomPanel.add(contentField, BorderLayout.NORTH);

        // 버튼 영역
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton imageUploadButton = new JButton("사진 업로드");
        styleButton(imageUploadButton, Color.BLUE);
        imageUploadButton.addActionListener(e -> uploadImage());

        uploadButton = new JButton("게시물 업로드");
        styleButton(uploadButton, Color.GRAY);
        uploadButton.setEnabled(false);
        uploadButton.addActionListener(e -> {
            if (selectedImage != null && !contentField.getText().isEmpty()) {
                mainScreen.addPost(contentField.getText(), selectedImage);
                JOptionPane.showMessageDialog(this, "게시물이 업로드되었습니다!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "사진과 내용을 모두 입력하세요!");
            }
        });

        buttonPanel.add(imageUploadButton);
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

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (file == null || !file.exists()) {
                JOptionPane.showMessageDialog(this, "유효한 파일을 선택하세요.", "파일 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 이미지 로드
            selectedImage = new ImageIcon(file.getAbsolutePath());

            // 이미지 크기 확인
            int imgWidth = selectedImage.getIconWidth();
            int imgHeight = selectedImage.getIconHeight();

            if (imgWidth <= 0 || imgHeight <= 0) {
                JOptionPane.showMessageDialog(this, "이미지 로드에 실패했습니다. 올바른 파일인지 확인하세요.", "이미지 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 기본 크기로 스케일링
            int width = 300;
            int height = 300; // 이미지 크기 확대
            Image scaledImage = selectedImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);

            // 스케일링된 이미지 설정
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.revalidate();
            imageLabel.repaint();

            // 업로드 버튼 활성화
            uploadButton.setEnabled(true);
            uploadButton.setBackground(Color.BLUE);
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
}
