package app.arcanum.crypto.aes;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import app.arcanum.crypto.ICrypto;
import app.arcanum.crypto.exceptions.*;

public class AesCrypto implements ICrypto {
	public final short KEY_SIZE = 256;
	public final short KEY_LENGTH = 32;
	public final short IV_LENGTH = 16;
	
	private final String ALGORITHM = "AES";
	private final String ALGORITHM_FULL = "AES/GCM/NoPadding";
	private final String ALGORITHM_RND = "SHA1PRNG";
	
	private byte[] _iv = new byte[IV_LENGTH];
	private byte[] _key = new byte[KEY_LENGTH];

	public AesCrypto(final Context context) {}

	@Override
	public void init() {}
	
	@Override
	public boolean isReady() { return true; }

	@Override
	public void waitForReady() {}
	
	@Override
	public byte[] encrypt(byte[] plaintext) throws CryptoException, EncryptException  {
		try {
			SecretKey key = generate_secure_key();
			IvParameterSpec iv = generate_secure_iv();
			
			Cipher cipher = Cipher.getInstance(ALGORITHM_FULL);
		    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		    
		    _iv 	= iv.getIV();
		    _key 	= key.getEncoded();
		    byte [] ciphertext = cipher.doFinal(plaintext);
		    return ciphertext;
		} catch (NoSuchAlgorithmException ex) {
			throw new EncryptException("NoSuchAlgorithmException", ex);
		} catch (NoSuchPaddingException ex) {
			throw new EncryptException("NoSuchPaddingException", ex);
		} catch (InvalidKeyException ex) {
			throw new EncryptException("InvalidKeyException", ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new EncryptException("InvalidAlgorithmParameterException", ex);
		} catch (IllegalBlockSizeException ex) {
			throw new EncryptException("IllegalBlockSizeException", ex);
		} catch (BadPaddingException ex) {
			throw new EncryptException("BadPaddingException", ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) throws CryptoException, DecryptException {
		try {
			SecretKey key = load_secure_key();
			IvParameterSpec iv = load_secure_iv();
			
			Cipher cipher = Cipher.getInstance(ALGORITHM_FULL);
		    cipher.init(Cipher.DECRYPT_MODE, key, iv);
		    
		    byte [] plaintext = cipher.doFinal(ciphertext);
			return plaintext;
		} catch (NoSuchAlgorithmException ex) {
			throw new DecryptException("NoSuchAlgorithmException", ex);
		} catch (NoSuchPaddingException ex) {
			throw new DecryptException("NoSuchPaddingException", ex);
		} catch (InvalidKeyException ex) {
			throw new DecryptException("InvalidKeyException", ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new DecryptException("InvalidAlgorithmParameterException", ex);
		} catch (IllegalBlockSizeException ex) {
			throw new DecryptException("IllegalBlockSizeException", ex);
		} catch (BadPaddingException ex) {
			throw new DecryptException("BadPaddingException", ex);
		}
	}
	
	public byte[] IV() {
		return _iv;
	}

	public void setIV(byte[] iv) {
		this._iv = iv;
	}

	public byte[] KEY() {
		return _key;
	}

	public void setKey(byte[] key) {
		this._key = key;
	}

	private SecretKey generate_secure_key() throws CryptoException {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
	        SecureRandom sr = SecureRandom.getInstance(ALGORITHM_RND);
	        kgen.init(KEY_SIZE, sr);
	        SecretKey skey = kgen.generateKey();
	        return skey;
		} catch(Exception ex) {
			throw new CryptoException("Unknown error while generating a secure key.", ex);
		}
	}
	
	private IvParameterSpec generate_secure_iv() throws CryptoException {
		try {
			byte[] iv = new byte[IV_LENGTH];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			IvParameterSpec spec = new IvParameterSpec(iv);
			return spec;
		} catch(Exception ex) {
			throw new CryptoException("Unknown error while generating a IV.", ex);
		}
	}
	
	private IvParameterSpec load_secure_iv() throws CryptoException {
		if(_iv == null)
			throw new CryptoException("No IV set.");
		if(_iv.length != IV_LENGTH)
			throw new CryptoException(String.format("IV must have a length of %1$s", IV_LENGTH));
		try {	
			IvParameterSpec spec = new IvParameterSpec(_iv);
			return spec;
		} catch(Exception ex) {
			throw new CryptoException("Unknown error while loading IV.", ex);
		}
	}

	private SecretKey load_secure_key() throws CryptoException {
		if(_key == null)
			throw new CryptoException("No secure key set.");
		if(_key.length != KEY_LENGTH)
			throw new CryptoException(String.format("Secure key must have a length of %1$s", KEY_LENGTH));
			
		try {
			SecretKey key = new SecretKeySpec(_key, 0, KEY_LENGTH, ALGORITHM);
			return key;
		} catch(Exception ex) {
			throw new CryptoException("Unknown error while loading secure key.", ex);
		}
	}
}
