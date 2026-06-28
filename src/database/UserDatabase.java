package database;
import security.PasswordHashService;
import security.FileValidator;
import java.io.File;
import java.sql.*;
public class UserDatabase {
	private static final String USER_STORAGE = System.getProperty("user.home") + File.separator + "FileManagerStorage";
	public static void register(String username, String password) throws Exception {
		username = FileValidator.validateUsername(username);
		try (Connection con = Database.getConnection()) {
			PreparedStatement check = con.prepareStatement("SELECT username FROM users WHERE username=?");
			check.setString(1, username);
			if (check.executeQuery().next()) {
				throw new Exception("User exists");
			}
			String hash = PasswordHashService.hashPassword(password);
			PreparedStatement ps = con.prepareStatement("INSERT INTO users(username,password) VALUES(?,?)");
			ps.setString(1, username);
			ps.setString(2, hash);
			ps.executeUpdate();
			File userFolder = new File(USER_STORAGE + File.separator + username);
			if(!userFolder.exists()) {
				userFolder.mkdirs();
			}
		}
	}
	public static boolean login(String username, String password) throws Exception {
		username = FileValidator.validateUsername(username);
		try (Connection con = Database.getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT password FROM users WHERE username=?");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) return false;
			return PasswordHashService.verify(password, rs.getString("password"));
		}
	}
}
