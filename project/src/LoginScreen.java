import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
	private String uid;

	private String serverAddress;
	private int serverPort;

	private ObjectOutputStream out;
	
	private Thread receiveThread = null;
	private Socket socket;

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

		p.add(b_login);

		// 로그인 버튼 클릭 시 메인 화면으로 이동
		b_login.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String clientId = t_id.getText().trim();
				String password = t_pw.getText();

				if (clientId.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(null, "아이디와 비밀번호를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
				} else {
					
					try {
						connectToServer();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					sendUserID();
					
					dispose(); // 로그인 창 닫기
					new MainScreen(LoginScreen.this, clientId); // 메인 화면 열기 (아이디 전달)
				}
			}
		});

		return p;
	}

	private void connectToServer() throws UnknownHostException, IOException {

		socket = new Socket();
		SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
		socket.connect(sa, 3000); // 3초가 넘어가도 연결이 안 되면 연결 시도 중단.

		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		receiveThread = new Thread(new Runnable() {
			private ObjectInputStream in;

			private void receiveMessage() {
				try {
					ChatMsg inMsg = (ChatMsg) in.readObject();
					if (inMsg == null) {
						disconnect();
						return;
					}

					switch (inMsg.mode) {
					case ChatMsg.MODE_TX_STRING:
						// printDisplay(inMsg.userID + ": " + inMsg.message);
						break;

					case ChatMsg.MODE_TX_IMAGE:
						// printDisplay(inMsg.userID + ": " + inMsg.message);
						// printDisplay(inMsg.image);
						break;
					}
				} catch (IOException e) {
					// printDisplay("연결을 종료했습니다.");
				} catch (ClassNotFoundException e) {
					// printDisplay("잘못된 객체가 전달되었습니다.");
				}
			}

			@Override
			public void run() {
				try {
					in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				} catch (IOException e) {
					
				}
				while (receiveThread == Thread.currentThread()) {
					receiveMessage();
				}
			}

		});
		receiveThread.start();
	}
	
	private void disconnect() {
		send(new ChatMsg(uid, ChatMsg.MODE_LOGOUT));

		try {
			receiveThread = null;
			socket.close();
		} catch (IOException e) {
			System.err.println("클라이언트 닫기 오류> " + e.getMessage());
			System.exit(-1);
		}
	}

	private void send(ChatMsg msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
		}
	}

	private void sendUserID() {

		uid = t_id.getText();
		send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
	}

	public static void main(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 54321;

		new LoginScreen(serverAddress, serverPort);

	}

}
