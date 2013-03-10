package app.arcanum.tasks.contracts;

import android.content.Context;

import app.arcanum.AppSettings;
import app.arcanum.crypto.SHA256;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
	public RegisterRequest(Context context) {
		AppSettings.init(context);
		String phone = AppSettings.getPhoneNumber();
		
		// Set public fields.
	    this.PhoneHash = SHA256.hash(phone);
	    this.PhoneType = String.format("Android%s:%s", android.os.Build.VERSION.SDK_INT, android.os.Build.VERSION.RELEASE);
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
