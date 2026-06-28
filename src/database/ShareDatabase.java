package database;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ShareDatabase {
	public static void share(String owner, String receiver, String file) throws Exception {
		try (Connection con = Database.getConnection()) {
			PreparedStatement check = con.prepareStatement("SELECT 1 FROM shared_files WHERE owner=? AND receiver=? AND filename=?");
			check.setString(1, owner);
			check.setString(2, receiver);
			check.setString(3, file);
			if (check.executeQuery().next()) return;
			PreparedStatement ps = con.prepareStatement("INSERT INTO shared_files(owner,receiver,filename) VALUES(?,?,?)");
			ps.setString(1, owner);
			ps.setString(2, receiver);
			ps.setString(3, file);
			ps.executeUpdate();
		}
	}
	public static boolean allowed(String user, String owner, String file) throws Exception {
		try (Connection con = Database.getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT 1 FROM shared_files WHERE owner=? AND receiver=? AND filename=?");
			ps.setString(1, owner);
			ps.setString(2, user);
			ps.setString(3, file);
			return ps.executeQuery().next();
		}
	}
	public static List<String> getSharedFiles(String receiver) throws Exception {
		List<String> result = new ArrayList<>();
		try(Connection con = Database.getConnection()) {
			PreparedStatement ps = con.prepareStatement("SELECT owner, filename FROM shared_files WHERE receiver=?");
			ps.setString(1, receiver);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				result.add(rs.getString("owner") + File.separator + rs.getString("filename"));
			}
		}
		return result;
	}
	public static void removeAccess(String receiver,String owner,String file) throws Exception {
		try(Connection con = Database.getConnection()){
			PreparedStatement ps = con.prepareStatement("DELETE FROM shared_files WHERE receiver=? AND owner=? AND filename=?");
			ps.setString(1, receiver);
			ps.setString(2, owner);
			ps.setString(3, file);
			ps.executeUpdate();
		}
	}
}
