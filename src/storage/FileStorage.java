package storage;
import java.io.File;
public class FileStorage {
	private static final String ROOT = System.getProperty("user.home") + File.separator +"FileManagerStorage";
	public static File getUserDir(String user) {
		File dir = new File(ROOT,user);
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}
	public static File resolve(String user, String filename) {
		return new File(getUserDir(user), filename);
	}
}