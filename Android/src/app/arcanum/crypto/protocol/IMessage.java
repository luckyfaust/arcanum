package app.arcanum.crypto.protocol;

import app.arcanum.crypto.exceptions.MessageProtocolException;

public interface IMessage {
	public final int VERSION = 1;
	
	String getSender();
	String getRecipient();
	byte[] getContent();
		
	IMessage fromBytes(byte[] msg) throws MessageProtocolException;
	byte[] toBytes() throws MessageProtocolException;
}
