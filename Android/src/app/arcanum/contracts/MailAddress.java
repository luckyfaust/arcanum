package app.arcanum.contracts;

import java.io.Serializable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import app.arcanum.crypto.SHA256;


public final class MailAddress implements Serializable {
	private static final long serialVersionUID = -2063458720388674009L;
	private String _mail;
	private String _hash;
	
	public MailAddress() {}
	
	public MailAddress(final String mailAddress) {
		this();
		setMail(mailAddress);
	}
	
	public String getHash() {
		return _hash;
	}
	
	public String getMail() {
		return _mail;
	}

	public void setMail(final String mailAddress) {
		_mail = mailAddress;
		_hash = SHA256.hash(_mail);
	}
	
	public boolean equals(final String mailAddress) {
		return _mail.equals(mailAddress);
	}
	
	public boolean equalsHash(final String mailAddressHash) {
		return _hash.equals(mailAddressHash);
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		return builder.append(_mail).append(_hash).toHashCode();
	}
}
