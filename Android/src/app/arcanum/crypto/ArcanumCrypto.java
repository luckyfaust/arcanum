package app.arcanum.crypto;

import app.arcanum.crypto.rsa.*;
import app.arcanum.crypto.aes.*;

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
}
