package security;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
public class KeyManager {
	private static final String KEY_STORAGE = System.getProperty("user.home") + File.separator + "FileManagerStorage";
	private static final String KEY_FILE = KEY_STORAGE + File.separator + "server.aes.key";
	private static SecretKey cachedKey;
	public static void init() {
		try {
			File folder = new File(KEY_STORAGE);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File keyFile = new File(KEY_FILE);
			if (keyFile.exists()) {
				cachedKey = loadKey();
				System.out.println("AES key loaded");
			} else {
				cachedKey = generateKey();
				saveKey(cachedKey);
				System.out.println("New AES key generated");
			}
		} catch(Exception e) {
			throw new RuntimeException("Key initialization failed",e);
		}
	}
	public static SecretKey getKey() {
		if(cachedKey == null) {
			init();
		}
		return cachedKey;
	}
	private static SecretKey generateKey() throws Exception {
		byte[] key = new byte[32];   // AES-256
		SecureRandom random = new SecureRandom();
		random.nextBytes(key);
		return new SecretKeySpec(key,"AES");
	}
	private static void saveKey(SecretKey key) throws Exception {
		try(FileOutputStream fos = new FileOutputStream(KEY_FILE)) {
			fos.write(key.getEncoded());
		}
	}
	private static SecretKey loadKey() throws Exception {
		byte[] data;
		try(FileInputStream fis = new FileInputStream(KEY_FILE)) {
			data = fis.readAllBytes();
		}
		return new SecretKeySpec(data,"AES");
	}
}