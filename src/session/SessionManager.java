package session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class SessionManager {
	private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
	public static void create(String token, String username) {
		sessions.put(token, new Session(username));
	}
	public static String valid(String token) {
		Session s = sessions.get(token);
		if (s == null || !s.valid()) {
			sessions.remove(token);
			throw new RuntimeException("SESSION_INVALID");
		}
		return s.username;
	}
	public static void remove(String token) {
		sessions.remove(token);
	}
}
