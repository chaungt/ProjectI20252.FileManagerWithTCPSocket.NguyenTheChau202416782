package security;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;
public class PasswordHashService {
	private static final int ITERATIONS=100000;
	private static final int KEY_LENGTH=256;
	public static String hashPassword(String password) throws Exception {
		byte[] salt = new byte[16];
		SecureRandom.getInstanceStrong().nextBytes(salt);
		byte[] hash = generate(password,salt);
		return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(hash);
	}
	public static boolean verify(String password,String stored) throws Exception {
		String[] data = stored.split(":");
		byte[] salt = Base64.getDecoder().decode(data[0]);
		byte[] oldHash = Base64.getDecoder().decode(data[1]);
		byte[] newHash = generate(password,salt);
		return Arrays.equals(oldHash,newHash);
	}
	private static byte[] generate(String password,byte[] salt) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),salt,ITERATIONS,KEY_LENGTH);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		return factory.generateSecret(spec).getEncoded();
	}
}