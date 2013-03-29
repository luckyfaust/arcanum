package app.arcanum.tasks.contracts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerContactResponse {
	@Expose
	@SerializedName("lookup_key")
	public String	LookupKey;
	
	@Expose
	@SerializedName("public_key")
	public String 	Pubkey;
	
	@Expose
	@SerializedName("hash")
	public String	PhoneHash;
}
