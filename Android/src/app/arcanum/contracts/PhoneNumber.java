package app.arcanum.contracts;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import app.arcanum.crypto.SHA256;
import app.arcanum.helper.PhoneNumberUtils;

public final class PhoneNumber implements Serializable {
	private static final long serialVersionUID = -1251146511500174599L;
	
	public PhoneNumber() {}
	
	public PhoneNumber(final String phone) {
		this();
		setPhone(phone);
	}

	private String _phone;
	private String _phoneCleaned;
	private String _hash;
	
	public String getHash() {
		return _hash;
	}	
	public String getPhone() {
		return _phone;
	}
	public String getPhoneCleaned() {
		return _phoneCleaned;
	}
	
	public void setPhone(final String phone) {
		_phone = phone;
		_phoneCleaned = PhoneNumberUtils.preparePhoneNumber(_phone);
		_hash = SHA256.hash(_phoneCleaned);		
	}
	
	public boolean equals(final String phone) {
		return _phoneCleaned.equals(PhoneNumberUtils.preparePhoneNumber(phone));
	}
	
	public boolean equalsHash(final String phoneHash) {
		return _hash.equals(phoneHash);
	}
		
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(_phoneCleaned).append(_hash);
		return builder.toHashCode();
	}	
}
