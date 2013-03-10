package app.arcanum.contacts;

import java.security.PublicKey;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ArcanumContact extends PossibleContact {
	private static final long serialVersionUID = 8489201983026816285L;
	
	public PublicKey 	Pubkey;
	public String 		Token;
	public String		AccName;
	
	@Override
	public int hashCode() {
		final HashCodeBuilder hashBuilder = new HashCodeBuilder();
		hashBuilder.append(AccName);
		hashBuilder.append(LookupKey);
		hashBuilder.append(Pubkey);
		return hashBuilder.toHashCode();
	}
}
