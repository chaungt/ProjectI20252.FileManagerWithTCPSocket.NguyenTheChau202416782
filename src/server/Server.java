package server;
import config.Config;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
public class Server {
	public static void main(String[] args) {
		try {
			Config.load("config/server.properties");
			int port = Config.getInt("port");
			System.setProperty("javax.net.ssl.keyStore", "config/server.jks");
			System.setProperty("javax.net.ssl.keyStorePassword", "chau55");
			System.setProperty("javax.net.ssl.keyStoreType", "JKS");
			SSLContext sslContext = SSLContext.getInstance("TLS");
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("config/server.jks"),"chau55".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keyStore, "chau55".toCharArray());
			sslContext.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);
			System.out.println("TLS Server started on port " + port);
			while (true) {
				Socket socket = serverSocket.accept();
				new ClientHandler(socket).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
