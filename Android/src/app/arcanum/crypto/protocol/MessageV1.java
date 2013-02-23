package app.arcanum.crypto.protocol;

import app.arcanum.crypto.exceptions.MessageProtocolException;

public class MessageV1 implements IMessage {
	public final int VERSION = 1;
	
	public byte[] From = new byte[32];
	public byte[] To = new byte[32];
	public byte[] IV = new byte[16];
	public byte[] Key = new byte[32];
	public byte[] Content;
		
	@Override
	public IMessage fromBytes(byte[] msg) throws MessageProtocolException {
		if(msg.length < 4+32+32+16+32+8+1)
			throw new MessageProtocolException("Message to short.");
		
		InputMessageStream stream  = new InputMessageStream(msg);
		try {
			if(this.VERSION != stream.readInt())
				throw new MessageProtocolException("No matching message version.");
				
			stream.read(From, 0, From.length);
			stream.read(To	, 0, To.length);
			stream.read(IV	, 0, IV.length);
			stream.read(Key	, 0, Key.length);
			
			int length = stream.readInt();
			Content = new byte[length];
			stream.read(Content, 0, length);
		} catch(java.io.IOException ex) {
			throw new MessageProtocolException("Message is corrupt.", ex);
		} finally {
			stream.close();
		}
		return this;
	}


	@Override
	public byte[] toBytes() throws MessageProtocolException {		
		OutputMessageStream stream = new OutputMessageStream();
		try {
			stream.writeInt(VERSION);
			stream.write(From);
			stream.write(To);
			stream.write(IV);
			stream.write(Key);
			stream.writeInt(Content.length);
			stream.write(Content);
		} catch(java.io.IOException ex) {
			throw new MessageProtocolException("Message writing failed.", ex);
		} finally {
			stream.close();
		}
		return stream.toByteArray();
	}	
}
