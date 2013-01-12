package app.arcanum.crypto.rsa;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import app.arcanum.crypto.ICrypto;
import app.arcanum.crypto.exceptions.*;
import app.arcanum.helper.StringUtils;

public class RsaCrypto implements ICrypto {
	final static String ALGORITHM = "RSA";
	final static String ALGORITHM_FULL = "RSA/NONE/PKCS1Padding";
	final short KEY_SIZE = 2048;
	
	private String _serverRequestUrl;
	private PublicKey _serverPublicKey;

	private PublicKey _publicKey;
	private PrivateKey _privateKey;

	public RsaCrypto(String connectionString) {
		_serverRequestUrl = connectionString;
	}
	
	@Override
	public void init() {
		try {
			load_serverPublicKey();
			generate_keys();
		} catch (Exception ex) {
			Log.e("FATAL: RSA", "Fatal error while init RSA!", ex);
		}
	}
	
	private void generate_keys() throws NoSuchAlgorithmException, NoSuchPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
		gen.initialize(KEY_SIZE);
		KeyPair keypair = gen.generateKeyPair();
		_publicKey = keypair.getPublic();
		_privateKey = keypair.getPrivate();
	}

	@Override
	public byte[] encrypt(byte[] plaintext) throws EncryptException {
		return encrypt(plaintext, _publicKey);
	}
	
	public byte[] encrypt_server(byte[] plaintext) throws EncryptException {
		return encrypt(plaintext, _serverPublicKey);
	}
	
	private static byte[] encrypt(byte[] plaintext, PublicKey key) throws EncryptException {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_FULL);
		    cipher.init(Cipher.ENCRYPT_MODE, key);
		    return cipher.doFinal(plaintext);
		} catch (NoSuchAlgorithmException ex) {
			throw new EncryptException("NoSuchAlgorithmException", ex);
		} catch (NoSuchPaddingException ex) {
			throw new EncryptException("NoSuchPaddingException", ex);
		} catch (InvalidKeyException ex) {
			throw new EncryptException("InvalidKeyException", ex);
		} catch (IllegalBlockSizeException ex) {
			throw new EncryptException("IllegalBlockSizeException", ex);
		} catch (BadPaddingException ex) {
			throw new EncryptException("BadPaddingException", ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) throws DecryptException {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_FULL);
			cipher.init(Cipher.DECRYPT_MODE, _privateKey);
			return cipher.doFinal(ciphertext);
		} catch (NoSuchAlgorithmException ex) {
			throw new DecryptException("NoSuchAlgorithmException", ex);
		} catch (NoSuchPaddingException ex) {
			throw new DecryptException("NoSuchPaddingException", ex);
		} catch (InvalidKeyException ex) {
			throw new DecryptException("InvalidKeyException", ex);
		} catch (IllegalBlockSizeException ex) {
			throw new DecryptException("IllegalBlockSizeException", ex);
		} catch (BadPaddingException ex) {
			throw new DecryptException("BadPaddingException", ex);
		}
	}
	
	public void load_serverPublicKey() {
		if(StringUtils.isNullOrWhitespace(_serverRequestUrl))
			return;
		
		try {
			AsyncTask<String, Void, PublicKey> task = new LoadServerPublickeyTask().execute(_serverRequestUrl);
			_serverPublicKey = task.get();
		} catch (InterruptedException ex) {
			Log.e("load_serverPublicKey", "InterruptedException", ex);
		} catch (ExecutionException ex) {
			Log.e("load_serverPublicKey", "ExecutionException", ex);
		}
	}
	
	public static PublicKey parsePublicKey(String publicKey) {
		// Cleanup
		publicKey = publicKey
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.trim();
			
		try {
			byte[] certBuf = Base64.decode(publicKey, Base64.DEFAULT);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(certBuf);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(spec);
		} catch (GeneralSecurityException ex) {
			Log.e("parsePublicKey", "Parsing public key failed!", ex);
		}
		return null;
	}
	
	private class LoadServerPublickeyTask extends AsyncTask<String, Void, PublicKey> {
		@Override
		protected PublicKey doInBackground(String... url) {
			try {
			    HttpClient client = new DefaultHttpClient();  
			    String getURL = _serverRequestUrl;
			    HttpGet get = new HttpGet(getURL);
			    HttpResponse responseGet = client.execute(get);  
			    HttpEntity resEntityGet = responseGet.getEntity();  
			    if (resEntityGet != null) {  
			        // do something with the response
			        String response = EntityUtils.toString(resEntityGet);
			        _serverPublicKey = RsaCrypto.parsePublicKey(response);
			    }
			} catch (Exception ex) {
			    Log.e("loadServerPublicKey", "Unknown error while getting the public key", ex);
			}
			return null;
		}
	}
}
