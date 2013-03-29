package app.arcanum.tasks.contracts;

import java.util.List;

import app.arcanum.AppSettings;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.SHA256;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageRequest {
	private MessageRequestOne _requestOne;
	
	public MessageRequest(final MessageRequestType type) {
		_requestOne = new MessageRequestOne();
		_requestOne.me = AppSettings.getPhoneNumber().getHash();
		_requestOne.contact = null;
		_requestOne.type = type;
		_requestOne.idsOnly = true;
	}
	
	public MessageRequest(final ArcanumContact contact, final MessageRequestType type) {
		_requestOne = new MessageRequestOne();
		_requestOne.me = AppSettings.getPhoneNumber().getHash();
		_requestOne.contact = SHA256.hash(contact.Token);
		_requestOne.type = type;
		_requestOne.idsOnly = true;
	}
	
	public MessageRequestOne getRequestOne() {
		return _requestOne;
	}
	
	public MessageRequestTwo getRequestTwo(List<Long> ids) {
		return new MessageRequestTwo(ids);
	}
	
	protected class MessageRequestOne {
		@Expose
		@SerializedName("me")
		public String me;

		@Expose
		@SerializedName("contact")
		public String contact;
		
		@Expose
		@SerializedName("type")
		public MessageRequestType type;
		
		@Expose
		@SerializedName("id_only")
		public boolean idsOnly;
	}
	
	protected class MessageRequestTwo {
		public MessageRequestTwo(List<Long> ids) {
			this.me = AppSettings.getPhoneNumber().getHash();
			this.type = MessageRequestType.IDS;
			this.ids = ids;
		}
		
		@Expose
		@SerializedName("me")
		public String me;

		@Expose
		@SerializedName("type")
		public MessageRequestType type;
		
		@Expose
		@SerializedName("id")
		public List<Long> ids;
	}
}
