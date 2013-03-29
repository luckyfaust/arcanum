package app.arcanum.ui.contracts;


import java.util.Date;
import app.arcanum.AppSettings;
import app.arcanum.contracts.MessageContentType;

public final class TextMessageItem extends MessageItem {
	private String _content;
	
	public TextMessageItem(long id, String sender, String recipient, Date date) {
		super(id, sender, recipient, date);
	}

	public String getContent() {
		return _content;
	}
	
	@Override
	public MessageContentType getContentType() {
		return MessageContentType.TEXT;
	}

	@Override
	public boolean setContent(Object content) {
		if(content instanceof byte[]) {
			_content = new String((byte[])content, AppSettings.ENCODING);
			return true;
		} 

		if(content instanceof String) {
			_content = (String)content;
			return true;
		} 
			
		return false;			
	}
}
