//2191181 최하정

import java.io.Serializable;

import javax.swing.ImageIcon;

public class ChatMsg implements Serializable{
	public final static int MODE_LOGIN = 0x1;
	public final static int MODE_LOGOUT = 0x2;
	public final static int MODE_TX_STRING = 0x10;
	public final static int MODE_TX_FILE = 0x20;
	public final static int MODE_TX_IMAGE = 0x40;
	
	String userID;
	int mode;
	String message;
	ImageIcon image;
	long size;
	
	public ChatMsg(String userID, int code, String message, ImageIcon image, long size) {
		this.userID = userID;
		this.mode =code;
		this.message = message;
		this.image = image;
		this.size = size;
	}
	
	public ChatMsg(String userID, int code, String message, ImageIcon image) {
		this(userID, code, message, image, 0);
	}
	
	public ChatMsg(String userID, int code) {
		this(userID, code, null, null);
	}
	
	public ChatMsg(String userID, int code, String message) {
		this(userID, code, message, null);
	}
	
	public ChatMsg(String userID, int code, ImageIcon image) {
		this(userID, code, null, image);
	}
	
	public ChatMsg(String userID, int code, String filename, long size) {
		this(userID, code, filename, null, size);
	}

}
