package app.arcanum.tasks.contracts;


import java.util.Date;

import app.arcanum.contracts.MessageContentType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {
	@Expose
	@SerializedName("key")
	public long Key;
	
	@Expose
	@SerializedName("version")
	public int Version;
	
	@Expose
	@SerializedName("sender")
	public String Sender;
	
	@Expose
	@SerializedName("recipient")
	public String Recipient;
	
	@Expose
	@SerializedName("content_type")
	public MessageContentType ContentType;
	
	@Expose
	@SerializedName("content")
	public String Content;
	
	@Expose
	@SerializedName("timestamp")
	public Date Timestamp;
}
