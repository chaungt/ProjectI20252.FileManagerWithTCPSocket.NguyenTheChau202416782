package server;
import database.Database;
import security.KeyManager;
import java.io.File;
public class ServerInstaller {
	public static void init() {
		try {
			System.out.println("[INSTALL] Starting server setup...");
			File storage = new File(System.getProperty("user.home"),"FileManagerTCPStorage");
			if (!storage.exists()) storage.mkdirs();
			KeyManager.init();
			Database.init();
			System.out.println("[INSTALL] Setup complete");
		} catch (Exception e) {
			throw new RuntimeException("Installation failed", e);
		}
	}
}
