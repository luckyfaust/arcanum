package app.arcanum.tasks.contracts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageRequest {
	public MessageRequest(final MessageRequestType type) {
		this.contact = null;
		this.type = type;
	}
	
	public MessageRequest(final String contact, final MessageRequestType type) {
		this.contact = contact;
		this.type = type;
	}

	@Expose
	@SerializedName("contact")
	public final String 				contact;
	
	@Expose
	@SerializedName("type")
	public final MessageRequestType 	type;
}
