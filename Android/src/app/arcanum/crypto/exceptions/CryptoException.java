package app.arcanum.crypto.exceptions;

public class CryptoException extends Exception {
	private static final long serialVersionUID = -8386690074727102753L;

	public CryptoException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}	
}
