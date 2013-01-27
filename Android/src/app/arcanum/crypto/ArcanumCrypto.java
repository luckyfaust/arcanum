package app.arcanum.crypto;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import android.os.AsyncTask;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.protocol.MessageV1;
import app.arcanum.crypto.rsa.*;
import app.arcanum.crypto.aes.*;
import app.arcanum.crypto.exceptions.CryptoException;
import app.arcanum.crypto.exceptions.MessageProtocolException;

public class ArcanumCrypto {
	final Charset message_encoding = Charset.forName("UTF-8");
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
		try {
			switch (version) {
				case 1:
				default:
					MessageV1 message = new MessageV1();
					//TODO: Get my own contact.
					message.From = hash("+49 700 12 34 567", version);
					message.To = hash(to.Token, version);
					
					// Encrypt message.
					message.Content = _aes.encrypt(msg.getBytes(message_encoding));
					message.IV 		= _aes.IV();
					message.Key		= _aes.KEY();
					
					return message.toBytes();
			}
		} catch(CryptoException ex) {
			throw new MessageProtocolException("Error while creating message.", ex);
		}
	}
	
	public byte[] hash(String value, int version) throws CryptoException {
		try {
			switch (version) {
				case 1:
				default:
					Charset encoding = Charset.forName("UTF-8");
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					return digest.digest(value.getBytes(encoding));
			}
		} catch(NoSuchAlgorithmException ex) {
			throw new CryptoException("Hashing failed.");
		}
	}
}
