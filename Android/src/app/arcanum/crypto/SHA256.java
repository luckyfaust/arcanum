package app.arcanum.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Base64;
import android.util.Log;
import app.arcanum.AppSettings;

public class SHA256 {
	private static final MessageDigest _digest = getMessageDigest();
	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException ex) { 
			Log.e("FATAL", "SHA256 not found!", ex);
		}
		return null;
	}

	public static String hash(String msg) {
		_digest.reset();
		_digest.update(msg.getBytes(AppSettings.ENCODING));
		return Base64.encodeToString(_digest.digest(), Base64.DEFAULT).trim();
	}
	
	public static byte[] hashToBytes(String msg) {
		_digest.reset();
		_digest.update(msg.getBytes(AppSettings.ENCODING));
		return _digest.digest();
	}
}
