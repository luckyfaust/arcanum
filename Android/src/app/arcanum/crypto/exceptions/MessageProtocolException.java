package app.arcanum.crypto.exceptions;

public class MessageProtocolException extends Exception {
	private static final long serialVersionUID = -3529024696067018403L;
	public MessageProtocolException(String detailMessage) {
		super(detailMessage);
	}
	public MessageProtocolException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}	
}
