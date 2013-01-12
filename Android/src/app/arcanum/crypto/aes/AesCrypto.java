package app.arcanum.crypto.aes;

import app.arcanum.crypto.ICrypto;
import app.arcanum.crypto.exceptions.*;

public class AesCrypto implements ICrypto {
	final String ALGORITHM = "AES";
	final String ALGORITHM_FULL = "AES/CBC/PKCS5Padding";
	final short KEY_SIZE = 32;

	@Override
	public void init() {
		generate_keys();
	}
	
	private void generate_keys() {
		
	}

	@Override
	public byte[] encrypt(byte[] plaintext) throws EncryptException  {
		return null;
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) throws DecryptException {
		return null;
	}
}
