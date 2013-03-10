package app.arcanum.contacts;

import java.io.Serializable;
import java.util.ArrayList;

public class PossibleContact implements Serializable {
	private static final long serialVersionUID = -3155635349753035063L;
	
	public PossibleContact() {
		this.PhoneNumbers = new ArrayList<String>();
	}
	public String				LookupKey;
	public String				DisplayName;
	public ArrayList<String> 	PhoneNumbers;
}
