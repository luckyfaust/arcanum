package app.arcanum.contacts;

import java.util.ArrayList;

public class PossibleContact {
	public PossibleContact() {
		this.PhoneNumbers = new ArrayList<String>();
	}
	public String				LookupKey;
	public String				DisplayName;
	public ArrayList<String> 	PhoneNumbers;
}
