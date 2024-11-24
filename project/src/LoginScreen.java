import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;

public class LoginScreen extends JFrame {
    private JTextField t_id;
    private JTextField t_pw;
    private JButton b_login;
    private String serverAddress;
    private int serverPort;

    public LoginScreen(String serverAddress, int serverPort) {
        super("Hansunggram");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(Color.WHITE);

        JLabel logo = new JLabel(new ImageIcon("Instagram.png"));
        JLabel appName = new JLabel("Hansunggram");
        appName.setFont(new Font("SansSerif", Font.BOLD, 20));

        p.add(logo);
        p.add(appName);

        return p;
    }

    private JPanel createCenterPanel() {
		JPanel p = new JPanel();
		p.setLayout(null);
		p.setBackground(Color.WHITE);

		JLabel id = new JLabel("아이디");
		id.setBounds(100, 160, 50, 30);
		t_id = new JTextField(20);
		t_id.setBorder(null); // 테두리 삭제
		t_id.setBackground(Color.LIGHT_GRAY);
		t_id.setBounds(170, 160, 100, 30);

		JLabel pw = new JLabel("비밀번호");
		pw.setBounds(100, 200, 50, 30);
		t_pw = new JTextField(20);
		t_pw.setBorder(null);
		t_pw.setBackground(Color.LIGHT_GRAY);
		t_pw.setBounds(170, 200, 100, 30);

		p.add(id);
		p.add(t_id);
		p.add(pw);
		p.add(t_pw);

		b_login = new JButton("로그인");
		b_login.setContentAreaFilled(false); // 버튼 배경색 투명
		b_login.setBorderPainted(false); // 버튼 테두리 삭제
		b_login.setBounds(180, 240, 80, 30);
		
        b_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clientId = t_id.getText().trim();
                String password = t_pw.getText();

                if (clientId.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "아이디와 비밀번호를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                } else {
                    dispose(); // 로그인 창 닫기
                    new MainScreen(clientId, serverAddress, serverPort); // 메인 화면 열기
                }
            }
        });

        p.add(b_login);
        return p;
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;
        new LoginScreen(serverAddress, serverPort);
    }
}
