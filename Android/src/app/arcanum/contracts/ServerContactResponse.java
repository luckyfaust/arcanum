package app.arcanum.contracts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerContactResponse extends ServerContactRequest {
	@Expose
	@SerializedName("public_key")
	public String 	Pubkey;
}
