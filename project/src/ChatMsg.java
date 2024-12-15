import java.io.Serializable;
import java.util.Vector;

import javax.swing.ImageIcon;

public class ChatMsg implements Serializable {
    public final static int MODE_LOGIN = 0x1;
    public final static int MODE_LOGOUT = 0x2;
    public final static int MODE_TX_STRING = 0x10;
    public final static int MODE_TX_FILE = 0x20;
    public final static int MODE_TX_IMAGE = 0x40;
    public final static int MODE_TX_POST = 0x50; // 게시물 전송 모드 추가
    public final static int MODE_REQUEST_POSTS = 0x60; // 게시물 요청
    public final static int MODE_TX_USER_LIST = 0x70; // 유저 목록 전송
    public final static int MODE_CREATE_CHAT_ROOM = 0x80; // 채팅방 생성 모드
    public static final int MODE_REQUEST_CHAT_ROOMS = 0x90; // 채팅방 목록 요청

    
    String userID;
    int mode;
    String message;
    ImageIcon image;
    long size;
    long timestamp; // 생성 시간 추가

    // 생성자 (모든 필드 초기화)
    public ChatMsg(String userID, int code, String message, ImageIcon image, long size) {
        this.userID = userID;
        this.mode = code;
        this.message = message;
        this.image = image;
        this.size = size;
        this.timestamp = System.currentTimeMillis(); // 현재 시간 저장
    }

    // 생성자 (size 제외)
    public ChatMsg(String userID, int code, String message, ImageIcon image) {
        this(userID, code, message, image, 0);
    }

    // 생성자 (이미지와 메시지 없이)
    public ChatMsg(String userID, int code) {
        this(userID, code, null, null);
    }

    // 생성자 (메시지와 코드만)
    public ChatMsg(String userID, int code, String message) {
        this(userID, code, message, null);
    }

    // 생성자 (이미지와 코드만)
    public ChatMsg(String userID, int code, ImageIcon image) {
        this(userID, code, null, image);
    }

    // 생성자 (파일명과 크기)
    public ChatMsg(String userID, int code, String filename, long size) {
        this(userID, code, filename, null, size);
    }
}
