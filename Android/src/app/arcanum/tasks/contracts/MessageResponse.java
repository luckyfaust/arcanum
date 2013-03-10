package app.arcanum.tasks.contracts;


import app.arcanum.contracts.MessageContentType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageResponse {
	@Expose
	@SerializedName("contact")
	public String contact;
	
	@Expose
	@SerializedName("content_type")
	public MessageContentType content_type;
	
	@Expose
	@SerializedName("content")
	public String content;
}
