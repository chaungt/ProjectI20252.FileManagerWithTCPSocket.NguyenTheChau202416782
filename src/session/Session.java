package session;
public class Session {
	String username;
	long expiry;
	Session(String username) {
		this.username = username;
        this.expiry = System.currentTimeMillis() + 30 * 60 * 1000;
	}
	boolean valid() {
    	return System.currentTimeMillis() < expiry;
    }
}
