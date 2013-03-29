package app.arcanum.ui.contracts;

import java.util.Date;
import app.arcanum.contracts.MessageContentType;
import app.arcanum.tasks.contracts.MessageResponse;

public abstract class MessageItem {
	private long _id;
	private Date _date;
	private String _sender;
	private String _recipient;
	
	public MessageItem(long id, String sender, String recipient, Date date) {
		this._id = id;
		this._sender = sender;
		this._recipient = recipient;
		this._date = date;
	}
		
	public long getIdentifier() {
		return _id;
	}

	public String getSender() {
		return _sender;
	}

	public String getRecipient() {
		return _recipient;
	}
	
	public Date getTimestamp() {
		return _date;
	}

	public abstract MessageContentType getContentType();
	
	public abstract boolean setContent(Object content);
	
	public static MessageItem newInstance(MessageResponse response) {
		return newInstance(response.ContentType, response.Key, response.Sender, response.Recipient, response.Timestamp);
	}
	
	public static MessageItem newInstance(MessageContentType contentType, long id, String sender, String recipient, Date date) {
		switch(contentType) {
			case TEXT:
				return new TextMessageItem(id, sender, recipient, date);
			default:
				return null;
		}
	}	
}
