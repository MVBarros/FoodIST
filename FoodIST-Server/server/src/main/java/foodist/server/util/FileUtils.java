package foodist.server.util;

public class FileUtils {

	public static String getFileFromPath(String path) {
		String[] split_path = path.split("/");
		int position = split_path.length - 1;
		return split_path[position];
	}
}
