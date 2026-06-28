package service;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import database.ShareDatabase;
import protocol.Protocol;
import storage.FileStorage;
public class DeleteService {
	public static void handle(DataInputStream in, DataOutputStream out, String user) throws Exception {
		String name = in.readUTF();
		if (name.contains("..") || name.contains("\0")) {
			out.writeUTF("INVALID_FILE");
			return;
		}
		if (name.contains("/")) {
			String[] parts = name.split("/", 2);
			String owner = parts[0];
			String file = parts[1];
			if (!ShareDatabase.allowed(user, owner, file)) {
				out.writeByte(Protocol.ERROR_NO_PERMISSION);
				return;
			}
			ShareDatabase.removeAccess(user, owner, file);
			out.writeUTF("SHARE_REMOVED");
			return;
		}
		File f = FileStorage.resolve(user, name);
		if (!f.exists()) {
			out.writeByte(Protocol.ERROR_NOT_FOUND);
			return;
		}
		out.writeByte(f.delete() ? Protocol.OK : Protocol.FAILED);
		out.flush();
	}
}
