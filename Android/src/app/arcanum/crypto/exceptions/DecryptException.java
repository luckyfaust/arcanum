package app.arcanum.crypto.exceptions;

public class DecryptException extends Exception {
	private static final long serialVersionUID = -587180321854184070L;

	public DecryptException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}	
}
