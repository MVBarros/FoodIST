package foodist.server;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Security {
	
	static File getPrivateKey() throws URISyntaxException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("server.pem");
        Path path = Paths.get(url.toURI());
        return path.toFile();
	}
	
	static File getPublicKey() throws URISyntaxException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("server.key");
        Path path = Paths.get(url.toURI());
        return path.toFile();
	}
}
