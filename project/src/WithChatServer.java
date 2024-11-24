import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class WithChatServer extends JFrame{
	private int port;
	private ServerSocket serverSocket = null;

	private Thread acceptThread = null;
	private Vector<ClientHandler> users = new Vector<ClientHandler>();

	private JTextArea t_display;
	private JButton b_connect, b_disconnect, b_exit;

	public WithChatServer(int Port) {
		super("With ChatServer");

		buildGUI();

		setSize(400, 300);
		setLocation(500, 0);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setVisible(true);

		this.port = Port;
	}

	private void buildGUI() {
		add(createDisplayPanel(), BorderLayout.CENTER);

		add(createControlPanel(), BorderLayout.SOUTH);
	}


	private JPanel createDisplayPanel() {
		JPanel p = new JPanel(new BorderLayout());

		t_display = new JTextArea();
		t_display.setEditable(false);

		p.add(new JScrollPane(t_display), BorderLayout.CENTER);

		return p;
	}

	private JPanel createControlPanel() {
		JPanel p = new JPanel(new GridLayout(1, 0));

		b_connect = new JButton("서버 시작");
		b_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// connectToServer();
				// startServer();

				acceptThread = new Thread(new Runnable() {

					@Override
					public void run() {
						startServer();
					}

				});
				acceptThread.start();

				b_connect.setEnabled(false);
				b_disconnect.setEnabled(true);

				// t_input.setEnabled(true);
				// b_send.setEnabled(true);
				b_exit.setEnabled(false);
			}
		});

		b_disconnect = new JButton("서버 종료");
		b_disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();

				b_connect.setEnabled(true);
				b_disconnect.setEnabled(false);

				// t_input.setEnabled(false);
				// b_send.setEnabled(false);
				b_exit.setEnabled(true);
			}
		});

		b_exit = new JButton("종료하기");
		b_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		p.add(b_connect);
		p.add(b_disconnect);
		p.add(b_exit);

		b_disconnect.setEnabled(false);

		return p;
	}

	private void disconnect() {
		try {
			acceptThread = null;
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("클라이언트 닫기 오류> " + e.getMessage());
			System.exit(-1);
		}
	}

	private void printDisplay(String msg) {
		t_display.append(msg + "\n");
		t_display.setCaretPosition(t_display.getDocument().getLength());
	}

	private String getLocalAddr() {
		InetAddress local = null;
		String addr = "";
		try {
			local = InetAddress.getLocalHost();
			addr = local.getHostAddress();
			System.out.println(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return addr;
	}

	private void startServer() {
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			printDisplay("서버가 시작되었습니다 : " + getLocalAddr());

			while (acceptThread == Thread.currentThread()) {
				clientSocket = serverSocket.accept();
				// printDisplay("클라이언트가 연결되었습니다.");

				String cAddr = clientSocket.getInetAddress().getHostAddress();
				t_display.append("클라이언트가 연결되었습니다: " + cAddr + "\n");

				// receiveMessages(clientSocket);
				ClientHandler cHandler = new ClientHandler(clientSocket);
				users.add(cHandler);
				cHandler.start();
			}

		} catch (SocketException e) {
			// System.out.println("서버 소켓 종료: " + e.getMessage());
			printDisplay("서버 소켓 종료");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (clientSocket != null)
					clientSocket.close();
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				System.err.println("서버 닫기 오류> " + e.getMessage());
				System.exit(-1);
			}
		}
	}


	private class ClientHandler extends Thread {
		private Socket clientSocket;

		private ObjectOutputStream out;

		private String uid;

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		private void receiveMessages(Socket cs) {
		    try {
		        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(cs.getInputStream()));
		        out = new ObjectOutputStream(new BufferedOutputStream(cs.getOutputStream()));

		        ChatMsg msg;
		        while ((msg = (ChatMsg) in.readObject()) != null) {
		            if (msg.mode == ChatMsg.MODE_LOGIN) {
		                uid = msg.userID;

		                printDisplay("새 참가자: " + uid);
		                printDisplay("현재 참가자 수: " + users.size());
		                continue;
		            } else if (msg.mode == ChatMsg.MODE_LOGOUT) {
		                break;
		            } else if (msg.mode == ChatMsg.MODE_TX_STRING) {
		                // 일반 텍스트 메시지 처리
		                String message = uid + ": " + msg.message;
		                printDisplay(message);
		                broadcasting(msg);
		            } else if (msg.mode == ChatMsg.MODE_TX_IMAGE) {
		                // 이미지 메시지 처리
		                printDisplay(uid + ": " + msg.message);
		                broadcasting(msg);
		            } else if (msg.mode == ChatMsg.MODE_TX_POST) {
		                // 게시물 메시지 처리
		                printDisplay(uid + " 게시물 업로드: " + msg.message);
		                broadcasting(msg);
		            }
		        }

		        users.removeElement(this);
		        printDisplay(uid + " 퇴장. 현재 참가자 수: " + users.size());

		    } catch (IOException e) {
		        users.removeElement(this);
		        printDisplay(uid + " 연결 끊김. 현재 참가자 수: " + users.size());
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		    } finally {
		        try {
		            cs.close();
		        } catch (IOException e) {
		            System.err.println("서버 닫기 오류> " + e.getMessage());
		            System.exit(-1);
		        }
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

		private void sendMessage(String msg) {
			
			send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING, msg));
		}

		private void broadcasting(ChatMsg msg) {
			for (ClientHandler c : users) {
				c.send(msg);
			}
		}

		@Override
		public void run() {
			receiveMessages(clientSocket);
		}

	}

	public static void main(String[] args) {
		int port = 54321;

		WithChatServer server = new WithChatServer(port);

	}

}
