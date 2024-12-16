import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		t_id.setBorder(null); //테두리 삭제
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
		b_login.setContentAreaFilled(false); //버튼 배경색 투명
		b_login.setBorderPainted(false); //버튼 테두리 삭제
		b_login.setBounds(180, 240, 80, 30);
		
		b_login.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String clientId = t_id.getText().trim();
		        String password = t_pw.getText();

		        if (clientId.isEmpty() || password.isEmpty()) {
		            JOptionPane.showMessageDialog(null, "아이디와 비밀번호를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
		            return;
		        }

		        try {
		            // 서버 연결
		            Socket socket = new Socket(serverAddress, serverPort);
		            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

		            // 로그인 메시지 전송
		            String loginMessage = clientId + "::" + password;
		            ChatMsg loginMsg = new ChatMsg(clientId, ChatMsg.MODE_LOGIN, loginMessage);
		            out.writeObject(loginMsg);
		            out.flush();

		            // 서버 응답 확인
		            ChatMsg responseMsg = (ChatMsg) in.readObject();
		            if ("로그인 성공".equals(responseMsg.message)) {
		                JOptionPane.showMessageDialog(null, "로그인 성공!");
		                dispose(); // 로그인 화면 닫기
		                new MainScreen(clientId, socket, out, in); // 소켓과 스트림 전달
		            } else {
		                JOptionPane.showMessageDialog(null, responseMsg.message, "로그인 실패", JOptionPane.ERROR_MESSAGE);
		                socket.close(); // 실패 시 소켓 종료
		            }
		        } catch (IOException | ClassNotFoundException ex) {
		            JOptionPane.showMessageDialog(null, "서버 연결 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
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
