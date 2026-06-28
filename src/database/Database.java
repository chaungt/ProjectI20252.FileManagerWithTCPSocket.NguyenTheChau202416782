package database;
import java.sql.*;
import config.Config;
public class Database {
	private static String URL;
	private static String USER;
	private static String PASSWORD;
	static {
		try {
			Config.load("config/server.properties");
			URL = Config.get("db.url");
			USER = Config.get("db.user");
			PASSWORD = Config.get("db.password");
		} catch(Exception e) {
			throw new RuntimeException("Database config load failed", e);
		}
	}
    //private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;"+"databaseName=FileManager;"+"encrypt=true;"+"trustServerCertificate=true";
    //private static final String USER = "sa";
    //private static final String PASSWORD ="nguyenthechau";
	public static Connection getConnection() throws Exception {
		return DriverManager.getConnection(URL,USER,PASSWORD);
	}
	public static void init() throws Exception {
		Connection con = getConnection();
		Statement st = con.createStatement();
		st.execute(
				"""
				IF NOT EXISTS
				(SELECT * FROM sysobjects WHERE name='users')
				CREATE TABLE users
				(id INT IDENTITY(1,1) PRIMARY KEY,username VARCHAR(100) UNIQUE,password VARCHAR(255))
				""");
        st.execute(
        		"""
        		IF NOT EXISTS
        		(SELECT * FROM sysobjects WHERE name='shared_files')
        		CREATE TABLE shared_files
        		(id INT IDENTITY(1,1) PRIMARY KEY,owner VARCHAR(100),receiver VARCHAR(100),filename VARCHAR(255))
        		""");
        con.close();
	}
}
