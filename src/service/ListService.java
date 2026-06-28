package service;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import database.ShareDatabase;
import storage.FileStorage;
public class ListService {
	public static void handle(DataOutputStream out, String user) throws Exception {
		List<String> files = new ArrayList<>();
		File dir = FileStorage.getUserDir(user);
		File[] own = dir.listFiles();
		if (own != null) {
			for (File f : own) {
				if (f.isFile()) {
					files.add(f.getName());
				}
			}
		}
		files.addAll(ShareDatabase.getSharedFiles(user));
        out.writeInt(files.size());
        for (String f : files) {
        	out.writeUTF(f);
        }
        out.flush();
	}
}