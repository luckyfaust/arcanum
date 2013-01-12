package app.arcanum.helper;

import java.io.InputStream;

public class StreamUtil {
	public static void closeSilent(InputStream is) {
		if (is == null) return;
		try { is.close(); } catch (Exception ign) {}
	}
}
