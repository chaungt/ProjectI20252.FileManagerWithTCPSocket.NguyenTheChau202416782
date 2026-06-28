package service;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import database.ShareDatabase;
import protocol.Protocol;
import security.KeyManager;
import security.StreamingEncryption;
import storage.FileStorage;
public class DownloadService {
	public static void handle(DataInputStream in, DataOutputStream out, String user) throws Exception {
		System.out.println("Handling download for: " + user);
		int count = in.readInt();
		List<File> result = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String name = in.readUTF();
			if (name.contains("..") || name.contains("\0")) {
				out.writeByte(Protocol.ERROR);
				out.writeByte(Protocol.ERROR_INVALID_FILE);
				out.flush();
				return;
			}
			File own = FileStorage.resolve(user, name);
			if (own.exists()) {
				result.add(own);
				continue;
			}
			if (name.contains("/")) {
				String[] p = name.split("/", 2);
				String owner = p[0];
				String file = p[1];
				if (!ShareDatabase.allowed(user, owner, file)) {
					out.writeByte(Protocol.ERROR);
					out.writeByte(Protocol.ERROR_NO_PERMISSION);
					out.flush();
					return;
				}
				File shared = FileStorage.resolve(owner, file);
				if (!shared.exists()) {
					out.writeByte(Protocol.ERROR);
					out.writeByte(Protocol.ERROR_NOT_FOUND);
					out.flush();
					return;
				}
				result.add(shared);
				continue;
			}
			out.writeByte(Protocol.ERROR);
			out.writeByte(Protocol.ERROR_NOT_FOUND);
			out.flush();
			return;
		}
		out.writeByte(Protocol.OK);
		out.writeInt(result.size());
		for (File f : result) {
			out.writeUTF(f.getName());
			File temp = File.createTempFile("download_", ".tmp");
			try (
					FileInputStream fis = new FileInputStream(f);
					FileOutputStream fos = new FileOutputStream(temp)
			) {
				StreamingEncryption.decrypt(fis, fos, KeyManager.getKey());
			}
			out.writeLong(temp.length());
			try (FileInputStream fis = new FileInputStream(temp)) {
				byte[] buffer = new byte[8192];
				int r;
				while ((r = fis.read(buffer)) != -1) {
					out.write(buffer, 0, r);
				}
			}
			temp.delete();
		}
		out.flush();
	}
}