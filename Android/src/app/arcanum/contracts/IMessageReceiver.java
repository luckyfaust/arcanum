package app.arcanum.contracts;

import app.arcanum.contacts.ArcanumContact;

public interface IMessageReceiver {
	public ArcanumContact getContact();
	public void pushMessage();
}
