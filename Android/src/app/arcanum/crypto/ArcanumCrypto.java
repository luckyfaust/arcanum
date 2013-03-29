package app.arcanum.crypto;

import java.nio.ByteOrder;

import android.content.Context;
import android.util.Log;
import app.arcanum.AppSettings;
import app.arcanum.R;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.aes.AesCrypto;
import app.arcanum.crypto.exceptions.CryptoException;
import app.arcanum.crypto.exceptions.DecryptException;
import app.arcanum.crypto.exceptions.MessageProtocolException;
import app.arcanum.crypto.protocol.IMessage;
import app.arcanum.crypto.protocol.MessageV1;
import app.arcanum.crypto.rsa.RsaCrypto;

public class ArcanumCrypto {
	private final static String TAG = "ArcanumCrypto";
	
	public final RsaCrypto RSA;
	public final AesCrypto AES;
	
	private final Context _context;
	
	
	public ArcanumCrypto(Context context) {
		_context = context;
		RSA = new RsaCrypto(context);
		AES = new AesCrypto(context);
		
		RSA.init();
		AES.init();
	}
	
	public byte[] create_message(ArcanumContact to, String msg) throws MessageProtocolException {
		return create_message(to, msg, 1);
	}
	
	public byte[] create_message(ArcanumContact to, byte[] content) throws MessageProtocolException {
		return create_message(to, content, 1);
	}
	
	public byte[] create_message(ArcanumContact to, String msg, int version) throws MessageProtocolException {
		final byte[] content = msg.getBytes(AppSettings.ENCODING);
		return create_message(to, content, version);
	}
	
	public byte[] create_message(ArcanumContact to, byte[] content, int version) throws MessageProtocolException {
		try {
			switch (version) {
				case 1:
				default:
					MessageV1 message = new MessageV1();
					message.From = SHA256.hashToBytes(AppSettings.getPhoneNumber().getPhoneCleaned());
					message.To = SHA256.hashToBytes(to.Token);
					
					// Encrypt message.
					message.Content = AES.encrypt(content);
					message.IV 		= AES.IV();
					message.Key		= AES.KEY();
					
					return message.toBytes();
			}
		} catch(CryptoException ex) {
			throw new MessageProtocolException("Error while creating message.", ex);
		}
	}
	
	public IMessage read_message(byte[] content) throws MessageProtocolException {
		try {
			java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(content, 0, 4);
			buffer.order(ByteOrder.BIG_ENDIAN);
			int version = buffer.getInt();
			
			switch (version) {
				case 1:
				default:
					MessageV1 message = new MessageV1();
					message.fromBytes(content);
					
					try {
					// Decrypt with AES, init with passed IV and Key.
						AES.setIV(message.IV);
						AES.setKey(message.Key);
						message.Content = AES.decrypt(message.Content);
					} catch(DecryptException ex) {
						Log.e(TAG , "Decrypt error while reading message.", ex);
						message.Content = _context
							.getString(R.string.message_status_cryptofail)
							.getBytes(AppSettings.ENCODING);
					}
					return message;
			}
		} catch(CryptoException ex) {
			throw new MessageProtocolException("FATAL Error while reading the message.", ex);
		}
	}
}
