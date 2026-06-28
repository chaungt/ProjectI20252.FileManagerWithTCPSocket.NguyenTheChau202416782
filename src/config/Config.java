package config;
import java.io.*;
import java.util.Properties;
public class Config {
	private static final Properties props = new Properties();
	private static File getConfigFile(String name) {
		String appDir = System.getProperty("user.dir") + File.separator;
		return new File(appDir, name);
	}
	public static void load(String file) throws Exception {
		File configFile = getConfigFile(file);
		if(!configFile.exists()) {
			throw new FileNotFoundException("Missing config: " + configFile.getAbsolutePath());
		}
		try(FileInputStream fis = new FileInputStream(configFile)) {
			props.load(fis);
		}
	}
	public static String get(String key) {
		return props.getProperty(key);
	}
	public static int getInt(String key) {
		return Integer.parseInt(props.getProperty(key));
	}
}
