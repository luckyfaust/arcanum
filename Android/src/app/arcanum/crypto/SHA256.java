package app.arcanum.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;
import android.util.Log;
import app.arcanum.AppSettings;


/**
 * Represents the {@link MessageDigest} with SHA256.
 */
public final class SHA256 {
	private static final MessageDigest _digest = getMessageDigest();
	private static MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA256");
		} catch(NoSuchAlgorithmException ex) {
			Log.e("FATAL", "SHA256 not found!", ex);
		}
		return null;
	}

	/**
	 * Computes and returns the final hash value, as an encoded base64 string,
	 * with sha256.
	 * 
	 * @param msg
	 *            String to compute the sha256 hash value.
	 * @return
	 *         Base64 encoded string of the passed string.
	 */
	public static String hash(String msg) {
		return hash(msg.getBytes(AppSettings.ENCODING));
	}
	
	/**
	 * Computes and returns the final hash value, as an encoded base64 string,
	 * with sha256.
	 * 
	 * @param bytesToHash
	 *            Bytes to compute the sha256 hash value.
	 * @return sha256 hash value, encoded with base64, based on passed bytes.
	 */
	public static String hash(final byte[] bytesToHash) {
		return Base64.encodeToString(hashToBytes(bytesToHash), AppSettings.BASE64_FLAGS);
	}

	/**
	 * Computes and returns the final hash value with sha256.
	 * 
	 * @param msg
	 *            String to compute the sha256 hash value.
	 * @return sha256 hash value, based on passed bytes.
	 */
	public static byte[] hashToBytes(final String msg) {
		return hashToBytes(msg.getBytes(AppSettings.ENCODING));
	}
	
	/**
	 * Computes and returns the final hash value with sha256.
	 * 
	 * @param bytesToHash
	 *            Bytes to compute the sha256 hash value.
	 * @return sha256 hash value, based on passed bytes.
	 */
	public static byte[] hashToBytes(final byte[] bytesToHash) {
		_digest.reset();
		_digest.update(bytesToHash);
		return _digest.digest();
	}
}
