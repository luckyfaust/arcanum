package app.arcanum.tasks.contracts;

import android.content.Context;

import app.arcanum.AppSettings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
	public RegisterRequest(Context context) {
		AppSettings.init(context);
		
		// Set public fields.
	    this.PhoneHash = AppSettings.getPhoneNumber().getHash();
	    this.PhoneType = AppSettings.PHONE_TYPE;
	    this.RegistrationIDs = new String[] { AppSettings.GCM.REGISTRATION_ID };
    	this.Pubkey = AppSettings.getCrypto().RSA.getPublicKey();
	}
	
	@Expose
	@SerializedName("hash")
	public String 	PhoneHash;
	
	@Expose
	@SerializedName("type")
	public String	PhoneType;
	
	@Expose
	@SerializedName("registration_ids")
	public String[] RegistrationIDs;
	
	@Expose
	@SerializedName("public_key")
	public String 	Pubkey;	
}
