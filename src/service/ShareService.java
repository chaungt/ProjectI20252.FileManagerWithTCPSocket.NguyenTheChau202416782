package service;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import database.ShareDatabase;
import protocol.Protocol;
import security.FileValidator;
import storage.FileStorage;
public class ShareService {
	public static void handle(DataInputStream in, DataOutputStream out, String user) throws Exception {
		String receiver = FileValidator.validateUsername(in.readUTF());
		String file = FileValidator.validate(in.readUTF());
		File f = FileStorage.resolve(user, file);
		if (!f.exists()) {
			out.writeByte(Protocol.ERROR_NOT_FOUND);
			return;
			}
		ShareDatabase.share(user, receiver, file);
		out.writeByte(Protocol.OK);
		out.flush();
	}
}
