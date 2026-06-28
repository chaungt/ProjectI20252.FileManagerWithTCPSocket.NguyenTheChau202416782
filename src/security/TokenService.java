package security;
import java.util.UUID;
public class TokenService {
	public static String generate(){
		return UUID.randomUUID().toString();
	}
}