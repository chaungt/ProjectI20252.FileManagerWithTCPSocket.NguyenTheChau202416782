package security;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.SecureRandom;
public class StreamingEncryption {
	private static final int IV_LEN = 12;
	private static final int TAG_LEN = 128;
	public static void encrypt(InputStream in, OutputStream out, SecretKey key) throws Exception {
		byte[] iv = new byte[IV_LEN];
		new SecureRandom().nextBytes(iv);
		out.write(iv);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
		try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
			byte[] buffer = new byte[8192];
			int r;
			while ((r = in.read(buffer)) != -1) {
				cos.write(buffer, 0, r);
			}
		}
	}
	public static void decrypt(InputStream in, OutputStream out, SecretKey key) throws Exception {
		byte[] iv = new byte[IV_LEN];
		int read = 0;
		while (read < IV_LEN) {
			int r = in.read(iv, read, IV_LEN - read);
			if (r < 0) throw new EOFException("Missing IV");
			read += r;
		}
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
		try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
			byte[] buffer = new byte[8192];
			int r;
			while ((r = cis.read(buffer)) != -1) {
				out.write(buffer, 0, r);
			}
		}
	}
}