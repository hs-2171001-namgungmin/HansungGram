import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class WithChatServer extends JFrame {
    private int port;
    private ServerSocket serverSocket = null;

    private Thread acceptThread = null;
    private Vector<ClientHandler> users = new Vector<ClientHandler>();
    private ConcurrentHashMap<String, ChatMsg> posts = new ConcurrentHashMap<>(); // 게시물 저장소

    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;

    private static final String POSTS_FILE = "saved_posts/posts_data.ser"; // 게시물 저장 파일 경로
    private Vector<String> chatRooms = new Vector<>(); // 채팅방 목록 저장

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
        b_connect.addActionListener(e -> {
            acceptThread = new Thread(this::startServer);
            acceptThread.start();
            b_connect.setEnabled(false);
            b_disconnect.setEnabled(true);
            b_exit.setEnabled(false);
        });

        b_disconnect = new JButton("서버 종료");
        b_disconnect.addActionListener(e -> {
            disconnect();
            b_connect.setEnabled(true);
            b_disconnect.setEnabled(false);
            b_exit.setEnabled(true);
        });

        b_exit = new JButton("종료하기");
        b_exit.addActionListener(e -> System.exit(0));

        p.add(b_connect);
        p.add(b_disconnect);
        p.add(b_exit);
        b_disconnect.setEnabled(false);

        return p;
    }

    private void disconnect() {
        savePosts(); // 서버 종료 시 게시물 저장
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            printDisplay("서버가 정상적으로 종료되었습니다.");
        } catch (IOException e) {
            System.err.println("서버 종료 중 오류 발생: " + e.getMessage());
        }
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    private String getLocalAddr() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            return local.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startServer() {
        loadPosts(); // 서버 시작 시 게시물 로드
        try {
            serverSocket = new ServerSocket(port);
            printDisplay("서버가 시작되었습니다 : " + getLocalAddr());

            while (acceptThread == Thread.currentThread()) {
                Socket clientSocket = serverSocket.accept();
                String cAddr = clientSocket.getInetAddress().getHostAddress();
                printDisplay("클라이언트가 연결되었습니다: " + cAddr);

                ClientHandler cHandler = new ClientHandler(clientSocket);
                users.add(cHandler);
                cHandler.start();
            }
        } catch (SocketException e) {
            printDisplay("서버 소켓 종료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPosts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(POSTS_FILE))) {
            posts = (ConcurrentHashMap<String, ChatMsg>) ois.readObject();
            printDisplay("게시물 데이터 로드 완료: " + posts.size() + "개의 게시물");
        } catch (FileNotFoundException e) {
            printDisplay("저장된 게시물이 없습니다.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("게시물 데이터 로드 중 오류 발생: " + e.getMessage());
        }
    }

    private void savePosts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(POSTS_FILE))) {
            oos.writeObject(posts);
            printDisplay("게시물 데이터 저장 완료");
        } catch (IOException e) {
            System.err.println("게시물 데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream out;
        private String uid;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            receiveMessages(clientSocket);
        }

        private void receiveMessages(Socket cs) {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(cs.getInputStream()))) {
                out = new ObjectOutputStream(new BufferedOutputStream(cs.getOutputStream()));
                ChatMsg msg;

                while ((msg = (ChatMsg) in.readObject()) != null) {
                    if (msg.mode == ChatMsg.MODE_LOGIN) {
                        uid = msg.userID;
                        printDisplay("새 참가자: " + uid);
                        printDisplay("현재 참가자 수: " + users.size());
                    } else if (msg.mode == ChatMsg.MODE_LOGOUT) {
                        break;
                    } else if (msg.mode == ChatMsg.MODE_TX_POST) {
                        savePost(msg);
                        broadcasting(msg);
                    } else if (msg.mode == ChatMsg.MODE_REQUEST_POSTS) {
                        sendPostsToClient(out);
                    }else if (msg.mode == ChatMsg.MODE_TX_USER_LIST) {
                        sendUserList(); //현재 유저 목록 반환
                    }else if (msg.mode == ChatMsg.MODE_CREATE_CHAT_ROOM) {

                    	if (!chatRooms.contains(msg.message)) { //중복 방지
                            chatRooms.add(msg.message);
                    	}
                    	printDisplay("새 채팅방 생성: " + msg.message);
                    	// 해당 채팅방 참가자들에게 채팅방 생성 메시지 전송
                        for (ClientHandler c : users) {
                            if (msg.message.contains(c.uid)) { //채팅방에 포함된 유저만 전송
                                try {
                                    c.out.writeObject(msg);
                                    c.out.flush();
                                } catch (IOException e) {
                                    System.err.println("채팅방 생성 메시지 전송 오류: " + e.getMessage());
                                }
                            }
                        }
                    }else if (msg.mode == ChatMsg.MODE_REQUEST_CHAT_ROOMS) {
                        sendChatRoomList(out); //현재 채팅방 목록 전송

                    }
                }

                users.remove(this);
                printDisplay(uid + " 퇴장. 현재 참가자 수: " + users.size());
            } catch (IOException | ClassNotFoundException e) {
                users.remove(this);
                printDisplay(uid + " 연결 끊김. 현재 참가자 수: " + users.size());
            }
        }
        private void sendUserList() {
            Vector<String> userList = new Vector<>();
            for (ClientHandler c : users) {
                if (c.uid != null) {  //uid가 null이 아닌 경우에만 추가
                    userList.add(c.uid);
                }
            }
            try {
                // 사용자 목록을 명확하게 전달하기 위해 ","로 구분된 문자열로 전송
                String userListMessage = String.join(", ", userList);
                ChatMsg userListMsg = new ChatMsg(uid, ChatMsg.MODE_TX_USER_LIST, userListMessage);
                out.writeObject(userListMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("유저 목록 전송 오류: " + e.getMessage());
            }
        }
        
        private void sendChatRoomList(ObjectOutputStream out) {
            try {
                String chatRoomList = String.join("::", chatRooms); //채팅방 목록 문자열 생성
                ChatMsg roomListMsg = new ChatMsg("server", ChatMsg.MODE_REQUEST_CHAT_ROOMS, chatRoomList);
                out.writeObject(roomListMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("채팅방 목록 전송 오류: " + e.getMessage());
            }
        }
        
        private void savePost(ChatMsg postMsg) {
            try {
                File postDir = new File("saved_posts");
                if (!postDir.exists()) postDir.mkdir();

                String imageFileName = postMsg.userID + "_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(postDir, imageFileName);
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(postMsg.image.getImage().toString().getBytes());
                }

                posts.put(imageFileName, postMsg);
                printDisplay("게시물 저장 완료: " + postMsg.message);

                savePosts(); // 게시물 데이터 저장
            } catch (IOException e) {
                System.err.println("게시물 저장 중 오류 발생: " + e.getMessage());
            }
        }

        private void sendPostsToClient(ObjectOutputStream clientOut) {
            try {
                // 게시물 데이터를 오래된 순서대로 정렬
                posts.values().stream()
                    .sorted((post1, post2) -> Long.compare(post1.timestamp, post2.timestamp)) // 오래된 순 정렬
                    .forEachOrdered(post -> {
                        try {
                            clientOut.writeObject(post); // 클라이언트로 전송
                            clientOut.flush();
                        } catch (IOException e) {
                            System.err.println("게시물 전송 중 오류 발생: " + e.getMessage());
                        }
                    });
            } catch (Exception e) {
                System.err.println("게시물 정렬 및 전송 중 오류 발생: " + e.getMessage());
            }
        }


        private void broadcasting(ChatMsg msg) {
            for (ClientHandler c : users) {
                try {
                    c.out.writeObject(msg);
                    c.out.flush();
                } catch (IOException e) {
                    System.err.println("브로드캐스트 중 오류 발생: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 54321;
        new WithChatServer(port);
    }
}