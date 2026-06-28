package service;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import protocol.Protocol;
import security.FileValidator;
import security.KeyManager;
import security.StreamingEncryption;
import storage.FileStorage;
public class UploadService {
	public static void handle(DataInputStream in, DataOutputStream out, String user) throws Exception {
		int count = in.readInt();
		File dir = FileStorage.getUserDir(user);
		for (int i = 0; i < count; i++) {
			String name = FileValidator.validate(in.readUTF());
			long size = in.readLong();
			File temp = new File(dir, name + ".tmp");
			File finalFile = FileStorage.resolve(user, name);
			try (FileOutputStream fos = new FileOutputStream(temp)) {
				byte[] buffer = new byte[8192];
				long remaining = size;
				while (remaining > 0) {
					int r = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
					if (r < 0) throw new EOFException("Upload interrupted");
					fos.write(buffer, 0, r);
					remaining -= r;
				}
			}
			try (
					FileInputStream fis = new FileInputStream(temp);
					FileOutputStream fos = new FileOutputStream(finalFile)
				) {
				StreamingEncryption.encrypt(fis, fos, KeyManager.getKey());
			}
			temp.delete();
		}
		out.writeByte(Protocol.OK);
		out.flush();
	}
}