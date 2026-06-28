package security;
import java.nio.file.*;
public class FileValidator {
	public static String validate(String filename) throws Exception {
		if (filename == null) throw new Exception("Invalid filename");
		Path path = Paths.get(filename).normalize();
		if (path.isAbsolute() || path.getNameCount() != 1 || filename.contains("\0") || filename.contains("..")) {
			throw new Exception("Invalid filename");
		}
		return filename;
	}
	public static String validateUsername(String username) throws Exception {
		if (username == null) throw new Exception("Invalid username");
		if (!username.matches("[a-zA-Z0-9_]{3,30}")) {
			throw new Exception("Invalid username");
		}
		return username;
	}
}
