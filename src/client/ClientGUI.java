package client;
import protocol.Protocol;
import config.Config;
import javax.net.ssl.*;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.io.File;
public class ClientGUI extends JFrame {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String token = "";
	private JTextArea log = new JTextArea();
	public ClientGUI() {
		setTitle("File Manager Client");
		setSize(650, 420);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		log.setEditable(false);
		add(new JScrollPane(log), BorderLayout.CENTER);
		JPanel panel = new JPanel();
		JButton loginBtn = new JButton("Login");
		JButton registerBtn = new JButton("Register");
		JButton uploadBtn = new JButton("Upload");
		JButton downloadBtn = new JButton("Download");
		JButton listBtn = new JButton("List");
		JButton shareBtn = new JButton("Share");
		JButton deleteBtn = new JButton("Delete");
		panel.add(loginBtn);
		panel.add(registerBtn);
		panel.add(uploadBtn);
		panel.add(downloadBtn);
		panel.add(listBtn);
		panel.add(shareBtn);
		panel.add(deleteBtn);
		add(panel, BorderLayout.SOUTH);
		loginBtn.addActionListener(e -> login());
		registerBtn.addActionListener(e -> register());
		uploadBtn.addActionListener(e -> upload());
		downloadBtn.addActionListener(e -> download());
		listBtn.addActionListener(e -> listFiles());
		shareBtn.addActionListener(e -> share());
		deleteBtn.addActionListener(e -> delete());
		connectServer();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private void connectServer() {
		try {
			Config.load("config/client.properties");
			String ip = Config.get("server.ip");
			int port = Config.getInt("server.port");
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null,new TrustManager[]{
					new X509TrustManager() {
						public void checkClientTrusted(X509Certificate[] x, String s) {}
						public void checkServerTrusted(X509Certificate[] x, String s) {}
						public X509Certificate[] getAcceptedIssuers() { return null; }
					}
			},new SecureRandom());
			SSLSocketFactory factory = ctx.getSocketFactory();
			socket = factory.createSocket(ip, port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			log("TLS Connected");
		} catch (Exception e) {
			log("Connection failed: " + e.getMessage());
		}
	}
	private void register() {
		try {
			String user = JOptionPane.showInputDialog("Username");
			String pass = JOptionPane.showInputDialog("Password");
			out.writeByte(Protocol.REGISTER);
			out.writeUTF(user);
			out.writeUTF(pass);
			out.flush();
			byte status = in.readByte();
			log(status == Protocol.OK ? "Register success" : "Register failed");
		} catch (Exception e) {
			log("Register error: " + e.getMessage());
		}
	}
	private void login() {
		try {
			String user = JOptionPane.showInputDialog("Username");
			String pass = JOptionPane.showInputDialog("Password");
			out.writeByte(Protocol.LOGIN);
			out.writeUTF(user);
			out.writeUTF(pass);
			out.flush();
			byte status = in.readByte();
			if (status == Protocol.OK) {
				token = in.readUTF();
				log("Login success");
			} else {
				log("Login failed");
			}
		} catch (Exception e) {
			log("Login error: " + e.getMessage());
		}
	}
	private void upload() {
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			File[] files = chooser.getSelectedFiles();
			out.writeByte(Protocol.UPLOAD);
			out.writeUTF(token);
			out.writeInt(files.length);
			for (File file : files) {
				out.writeUTF(file.getName());
				out.writeLong(file.length());
				try (FileInputStream fis = new FileInputStream(file)) {
					byte[] buf = new byte[8192];
					int r;
					while ((r = fis.read(buf)) != -1) {
						out.write(buf, 0, r);
					}
				}
			}
			out.flush();
			log("Upload complete");
		} catch (Exception e) {
			log("Upload error: " + e.getMessage());
		}
	}
	private void download() {
		try {
			String name = JOptionPane.showInputDialog("File name");
			if (name == null || name.isBlank()) return;
			out.writeByte(Protocol.DOWNLOAD);
			out.writeUTF(token);
			out.writeInt(1);
			out.writeUTF(name);
			out.flush();
			byte status = in.readByte();
			if (status == Protocol.ERROR) {
				byte err = in.readByte();
				switch (err) {
					case Protocol.ERROR_NOT_FOUND -> log("File not found");
					case Protocol.ERROR_NO_PERMISSION -> log("No permission");
					case Protocol.ERROR_INVALID_FILE -> log("Invalid file");
					default -> log("Unknown error: " + err);
				}
				return;
			}
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				String fname = in.readUTF();
				long len = in.readLong();
				JFileChooser chooser = new JFileChooser();
				chooser.setSelectedFile(new File(fname));
				if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
					log("Download canceled");
					return;
				}
				File outFile = chooser.getSelectedFile();
				try (FileOutputStream fos = new FileOutputStream(outFile)) {
					byte[] buf = new byte[8192];
					long rem = len;
					while (rem > 0) {
						int r = in.read(buf, 0, (int) Math.min(buf.length, rem));
						fos.write(buf, 0, r);
						rem -= r;
					}
				}
				log("Downloaded: " + outFile.getAbsolutePath());
			}
		} catch (Exception e) {
			log("Download error: " + e.getMessage());
		}
	}
	private void listFiles() {
		try {
			out.writeByte(Protocol.LIST);
			out.writeUTF(token);
			out.flush();
			int size = in.readInt();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				sb.append(in.readUTF()).append("\n");
			}
			log(sb.toString());
		} catch (Exception e) {
			log("List error: " + e.getMessage());
		}
	}
	private void share() {
		try {
			String receiver = JOptionPane.showInputDialog("Receiver");
			String file = JOptionPane.showInputDialog("File");
			out.writeByte(Protocol.SHARE);
			out.writeUTF(token);
			out.writeUTF(receiver);
			out.writeUTF(file);
			out.flush();
			log("Share done");
		} catch (Exception e) {
			log("Share error: " + e.getMessage());
		}
	}
	private void delete() {
		try {
			String file = JOptionPane.showInputDialog("File");
			out.writeByte(Protocol.DELETE);
			out.writeUTF(token);
			out.writeUTF(file);
			out.flush();
			log("Delete done");
		} catch (Exception e) {
			log("Delete error: " + e.getMessage());
		}
	}
	private void log(String msg) {
		log.append(msg + "\n");
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ClientGUI gui = new ClientGUI();
			gui.setVisible(true);
		});
	}
}
