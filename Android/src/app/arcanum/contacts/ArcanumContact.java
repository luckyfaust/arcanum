package app.arcanum.contacts;

import java.security.PublicKey;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import app.arcanum.contracts.PhoneNumber;
import app.arcanum.crypto.rsa.RsaCrypto;

public class ArcanumContact extends PossibleContact {
	private static final long serialVersionUID = 8489201983026816285L;
	
	public String 	AccName;
	public String 	Pubkey;
	public String 	Token;
	
	private PublicKey _publicKey;
	
	@Override
	public int hashCode() {
		final HashCodeBuilder hashBuilder = new HashCodeBuilder();
		hashBuilder
			.append(AccName)
			.append(LookupKey)
			.append(Pubkey)
			.append(Token);
		return hashBuilder.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != getClass()) return false;
		
		ArcanumContact contact = (ArcanumContact)obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder
			.append(AccName, contact.AccName)
			.append(LookupKey, contact.LookupKey)
			.append(Pubkey, contact.Pubkey)
			.append(Token, contact.Token);
		return builder.isEquals();
	}

	/**
	 * Sets or updates the current {@link PublicKey} as {@link String}.
	 * @param publicKey The {@link PublicKey} that will be set.
	 * @return Returns {@link true}, if the passed {@link PublicKey} is set; otherwise {@link false}.
	 */
	public boolean updatePubkey(String publicKey) {
		if(publicKey.equals(Pubkey))
			return false;
		
		this.Pubkey = publicKey;
		return true;
	}
	
	public void updateToken(String phoneHash) {
		for(PhoneNumber phone : PhoneNumbers) {
			if(phone.equalsHash(phoneHash)) {
				Token = phone.getPhoneCleaned();
				return;
			}
		}
	}
	
	public PublicKey getPublicKey() {
		if(_publicKey == null) {
			_publicKey = RsaCrypto.parsePublicKey(Pubkey);
		}
		return _publicKey;
	}
	
	public boolean isValid() {
		return StringUtils.isNotBlank(Pubkey)
			&& StringUtils.isNotBlank(Token);
	}
}
