package app.arcanum.services;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import app.arcanum.AppSettings;
import app.arcanum.MessageActivity;
import app.arcanum.R;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.IMessageReceiver;
import app.arcanum.contracts.MessageContentType;
import app.arcanum.crypto.exceptions.MessageProtocolException;
import app.arcanum.tasks.HttpGetMessagesTask;
import app.arcanum.tasks.HttpSendMessageTask;
import app.arcanum.tasks.contracts.MessageRequest;
import app.arcanum.tasks.contracts.MessageRequestType;
import app.arcanum.tasks.contracts.MessageResponse;

public class MessageService extends Service {
	private final String TAG = "MessageService";
	
	private final IBinder _binder = new MessageBinder();
	private final BroadcastReceiver _messageReceiver = new MessageReceiver();
	private final ArrayList<IMessageReceiver> _subscribers = new ArrayList<IMessageReceiver>();
	
	@Override
	public void onCreate() {
		super.onCreate();		
		LocalBroadcastManager
			.getInstance(this)
			.registerReceiver(_messageReceiver, new IntentFilter(AppSettings.Broadcasts.MESSAGE_RECEIVED));
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager
			.getInstance(this)
			.unregisterReceiver(_messageReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Service is not automatically restarted if terminated by the Android system.
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}
	
	public boolean sendMessage(final ArcanumContact to, final String message) {
		return sendMessage(to, message.getBytes(AppSettings.ENCODING), MessageContentType.TEXT);
	}
	
	public boolean sendMessage(final ArcanumContact to, final byte[] content, final MessageContentType type) {
		try {
			final byte[] cipherMessage = AppSettings.getCrypto().create_message(to, content);
			new HttpSendMessageTask()
				//.setCallback() // TODO: Set Callback for better status report. 
				.execute(cipherMessage);
			return true;
		} catch (MessageProtocolException ex) {
			Log.e(TAG, "Sending a message failed.");
			return false;
		}
	}
	
	/**
	 * Returns all messages, from all users, for the current user.
	 * @return
	 */
	public MessageResponse[] getMessages() {
		MessageRequest request = new MessageRequest(MessageRequestType.ALL);		
		return exec_getMessages(request);
	}
	
	/**
	 * Returns all message, from passed {@link ArcanumContact}, for the current user.
	 * @param contact
	 * @return
	 */
	public MessageResponse[] getMessages(final ArcanumContact contact) {
		MessageRequest request = new MessageRequest(contact.Token, MessageRequestType.ALL_CONTACT);
		return exec_getMessages(request);
	}
	
	/**
	 * Returns all unreaded messages, from all users,  for the current user.
	 * @return
	 */
	public MessageResponse[] getUnreadMessages() {
		MessageRequest request = new MessageRequest(MessageRequestType.UNREAD);		
		return exec_getMessages(request);
	}
	
	/**
	 * Return all unreaded messages, from passed {@link ArcanumContact}, for the current user.
	 * @param contact
	 * @return
	 */
	public MessageResponse[] getUnreadMessages(final ArcanumContact contact) {
		MessageRequest request = new MessageRequest(contact.Token, MessageRequestType.UNREAD_CONTACT);
		return exec_getMessages(request);
	}
	
	private MessageResponse[] exec_getMessages(MessageRequest request) {
		try {
			return new HttpGetMessagesTask()
				.execute(request)
				.get();
		} catch (InterruptedException ex) {
			Log.e(TAG, "InterruptedException", ex);
		} catch (ExecutionException ex) {
			Log.e(TAG, "ExecutionException", ex);
		}
		return null;		
	}
	
	public void registerMessageActivity(IMessageReceiver receiver) {
		if(_subscribers.contains(receiver))
			return;
		
		if(_subscribers.add(receiver))
			Log.i(TAG, "IMessageReceiver registered.");
		else
			Log.e(TAG, "IMessageReceiver couldn't be added.");
	}

	public void unregisterMessageActivity(IMessageReceiver receiver) {
		if(_subscribers.size() > 0 && _subscribers.remove(receiver))
			Log.i(TAG, "IMessageReceiver registration removed.");
		else
			Log.e(TAG, "IMessageReceiver couldn't be removed.");
	}
	
	public class MessageBinder extends Binder {
		public MessageService getService() {
			return MessageService.this;
		}
	}
	
	private class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if(bundle == null)
				return;
			
			final ArcanumContact sender = (ArcanumContact)bundle.get("sender");
			final String type = bundle.getString("type");
			
			if(sender == null)
				return;
			
			// Send to Activity or create Notification.
			boolean activityFound = false;
			for (IMessageReceiver receiver : _subscribers) {
				if(sender == receiver.getContact()) {
					receiver.pushMessage();
					activityFound = true;
					break;
				}
			}
			
			if(!activityFound)
				createNotification(sender, type);
		}
		
		private void createNotification(final ArcanumContact sender, final String type) {
	    	// Creating a Notification
	    	// http://developer.android.com/guide/topics/ui/notifiers/notifications.html#CreateNotification
	    	Intent msgIntent = new Intent(MessageService.this, MessageActivity.class);
	    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(MessageService.this);
	    	stackBuilder.addParentStack(MessageActivity.class);
	    	stackBuilder.addNextIntent(msgIntent);
	    	PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	    	
	    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MessageService.this)
		        .setSmallIcon(R.drawable.ic_launcher) // required
		        .setContentTitle(GetContentTitleFromType(type)) // required
		        .setContentText(GetContentTextFromType(type, sender)) // required
		        .setContentIntent(msgPendingIntent)
		        .setAutoCancel(true);
	    	
	    	NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	    	mNotificationManager.notify(type, sender.LookupKey.hashCode(), mBuilder.build());
		}
		
	    private String GetContentTitleFromType(String type) {
	    	if(type == "message")
	    		return getString(R.string.notfiy_new_message);
	    	return getString(R.string.app_name);
		}
	    
	    private String GetContentTextFromType(String type, Object... additional) {
	    	if(type == "message")
	    		return getString(R.string.notfiy_new_message_detail, additional);
	    	return getString(R.string.app_name);
		}
	}
}
