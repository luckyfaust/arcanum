package app.arcanum.helper;

public class StringUtils {
	public static boolean isNullOrWhitespace(String str) {
		return str == null || str.trim().isEmpty();
	}
}
