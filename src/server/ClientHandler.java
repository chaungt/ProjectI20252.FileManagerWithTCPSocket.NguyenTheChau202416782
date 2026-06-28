package server;
import protocol.Protocol;
import service.*;
import database.UserDatabase;
import security.TokenService;
import session.SessionManager;
import java.io.*;
import java.net.Socket;
public class ClientHandler extends Thread {
	private final Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	public ClientHandler(Socket socket) {
		this.socket = socket;
	}
	@Override
	public void run() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			while (true) {
				byte cmd;
				try {
					cmd = in.readByte();
				} catch (EOFException e) {
					break;
				}
				switch (cmd) {
					case Protocol.REGISTER -> {
						String u = in.readUTF();
                        String p = in.readUTF();
                        UserDatabase.register(u, p);
                        out.writeByte(Protocol.OK);
					}
					case Protocol.LOGIN -> {
						String u = in.readUTF();
						String p = in.readUTF();
						boolean ok = UserDatabase.login(u, p);
						if (ok) {
							String token = TokenService.generate();
							SessionManager.create(token, u);
							out.writeByte(Protocol.OK);
							out.writeUTF(token);
						} else {
							out.writeByte(Protocol.ERROR);
							out.writeByte(Protocol.ERROR_INVALID_FILE);
						}
					}
					case Protocol.UPLOAD -> {
						String user = SessionManager.valid(in.readUTF());
						UploadService.handle(in, out, user);
					}
					case Protocol.DOWNLOAD -> {
						String user = SessionManager.valid(in.readUTF());
						DownloadService.handle(in, out, user);
                    }
					case Protocol.LIST -> {
						String user = SessionManager.valid(in.readUTF());
						ListService.handle(out, user);
					}
					case Protocol.DELETE -> {
						String user = SessionManager.valid(in.readUTF());
						DeleteService.handle(in, out, user);
					}
					case Protocol.SHARE -> {
						String user = SessionManager.valid(in.readUTF());
						ShareService.handle(in, out, user);
					}
					default -> {
						out.writeByte(Protocol.ERROR);
						out.writeByte(Protocol.ERROR_UNKNOWN);
					}
				}
				out.flush();
			}
		} catch (Exception e) {
			System.out.println("Client disconnected: " + e.getMessage());
		}
	}
}