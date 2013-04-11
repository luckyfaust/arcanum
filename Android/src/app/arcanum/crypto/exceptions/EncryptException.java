package app.arcanum.crypto.exceptions;

public class EncryptException extends CryptoException {
	private static final long serialVersionUID = 3543235948929876669L;

	public EncryptException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
