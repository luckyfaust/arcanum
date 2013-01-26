package app.arcanum.crypto;

import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.protocol.MessageV1;
import app.arcanum.crypto.rsa.*;
import app.arcanum.crypto.aes.*;
import app.arcanum.crypto.exceptions.MessageProtocolException;

public class ArcanumCrypto {
	final RsaCrypto _rsa;
	final AesCrypto _aes;
	
	public ArcanumCrypto() {
		_rsa = new RsaCrypto("http://arcanum-app.appspot.com/auth");
		_aes = new AesCrypto();
		
		_rsa.init();
		_aes.init();
	}
	
	public RsaCrypto get_rsa() {
		return _rsa;
	}

	public AesCrypto get_aes() {
		return _aes;
	}
	
	public byte[] create_message(ArcanumContact to, String msg) throws MessageProtocolException {
		return create_message(to, msg, 1);
	}
	
	public byte[] create_message(ArcanumContact to, String msg, int version) throws MessageProtocolException {
		switch (version) {
		case 1:
		default:
			MessageV1 message = new MessageV1();
			return message.toBytes();
		}
	}
}
