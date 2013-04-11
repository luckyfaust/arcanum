package app.arcanum.contacts;

import java.io.Serializable;
import java.util.ArrayList;
import app.arcanum.contracts.MailAddress;
import app.arcanum.contracts.PhoneNumber;


public class PossibleContact implements Serializable {
	private static final long serialVersionUID = -3155635349753035063L;
	
	public PossibleContact() {
		this.PhoneNumbers = new ArrayList<PhoneNumber>();
		this.MailAddresses = new ArrayList<MailAddress>();
	}
	
	public String LookupKey;
	public String DisplayName;
	public ArrayList<PhoneNumber> PhoneNumbers;
	public ArrayList<MailAddress> MailAddresses;
}
