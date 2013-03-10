package app.arcanum.ui.contracts;

import app.arcanum.AppSettings;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.MessageContentType;

public class MessageOutgoing {
	public final String From;
	public final ArcanumContact To;
	public final byte[] Content;
	public final MessageContentType ContentType;
	public final MessageType Type = MessageType.OUTGOING;
	
	public MessageOutgoing(final ArcanumContact to, final byte[] content, final MessageContentType contentType) {
		this.From = AppSettings.getPhoneNumber();
		this.To = to;
		this.Content = content;
		this.ContentType = contentType;
	}
	
	public MessageOutgoing(final String from, final ArcanumContact to, final byte[] content, final MessageContentType contentType) {
		this.From = from;
		this.To = to;
		this.Content = content;
		this.ContentType = contentType;
	}
}
