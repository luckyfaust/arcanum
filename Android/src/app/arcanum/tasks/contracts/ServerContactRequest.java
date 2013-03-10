package app.arcanum.tasks.contracts;

import java.util.ArrayList;

import app.arcanum.crypto.SHA256;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerContactRequest {
	public ServerContactRequest() {
		this.PhoneHashes 	= new ArrayList<String>();
	}
	@Expose
	@SerializedName("lookup_key")
	public String				LookupKey;
	
	@Expose
	@SerializedName("phones")
	protected ArrayList<String>	PhoneHashes;
	
	public void addPhone(String phone) {
		PhoneHashes.add(SHA256.hash(phone));
	}
}
