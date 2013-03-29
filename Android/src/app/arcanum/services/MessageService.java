package app.arcanum.services;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import app.arcanum.AppSettings;
import app.arcanum.MessageActivity;
import app.arcanum.R;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.IMessageReceiver;
import app.arcanum.contracts.ISO8601DateFormat;
import app.arcanum.contracts.MessageContentType;
import app.arcanum.crypto.exceptions.MessageProtocolException;
import app.arcanum.crypto.protocol.IMessage;
import app.arcanum.providers.MessageContentProvider;
import app.arcanum.providers.db.tables.MessageTable;
import app.arcanum.tasks.HttpSendMessageTask;
import app.arcanum.tasks.contracts.ITaskPostListener;
import app.arcanum.tasks.contracts.MessageRequest;
import app.arcanum.tasks.contracts.MessageRequestType;
import app.arcanum.tasks.contracts.MessageResponse;

public class MessageService extends Service implements ITaskPostListener {
	private final static String TAG = MessageService.class.getSimpleName();
	
	private final ISO8601DateFormat _dateFormat = new ISO8601DateFormat(); 
	private final IBinder _binder = new MessageBinder();
	private final BroadcastReceiver _messageReceiver = new MessageReceiver();
	private final ArrayList<IMessageReceiver> _subscribers = new ArrayList<IMessageReceiver>();
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Creating");
		super.onCreate();
		LocalBroadcastManager
			.getInstance(this)
			.registerReceiver(_messageReceiver, new IntentFilter(AppSettings.Broadcasts.MESSAGE_RECEIVED));
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying");
		LocalBroadcastManager
			.getInstance(this)
			.unregisterReceiver(_messageReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Getting start command.");
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
		Log.i(TAG, String.format("Sending a new message to %s with %s.", to.Token, type.toString()));
		try {
			final byte[] cipherMessage = AppSettings.getCrypto().create_message(to, content);
			new HttpSendMessageTask()
				.setCallback(this)
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
		MessageRequest request = new MessageRequest(contact, MessageRequestType.ALL_CONTACT);
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
		MessageRequest request = new MessageRequest(contact, MessageRequestType.UNREAD_CONTACT);
		return exec_getMessages(request);
	}
	
	private MessageResponse[] exec_getMessages(MessageRequest request) {
		try {
			Log.i(TAG, "Starting now a task to get messages.");
			MessageResponse[] messages = new MessageGetTask()
				.setCallback(this)
				.execute(request)
				.get();
			return messages;
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
	
	@Override
	public void onPostExecute(String taskname, Object input, Object result) {
		if(taskname.equals(MessageGetTask.class.getName())) {
			MessageResponse[] messages = (MessageResponse[])result;
			
			for (MessageResponse msg : messages) {
				IMessage message    = null;
				try {
					byte[] byte_content = Base64.decode(msg.Content, Base64.DEFAULT);
					message = AppSettings.getCrypto().read_message(byte_content);
				} catch (MessageProtocolException ex) {
					Log.e(TAG, String.format("Message %d is not readable.", msg.Key), ex);
				}
				
				ContentValues values = new ContentValues();
				values.put(MessageTable.COLUMN_KEY, msg.Key);
				values.put(MessageTable.COLUMN_DATE, _dateFormat.format(msg.Timestamp));
				values.put(MessageTable.COLUMN_CONTENT_RAW, msg.Content);
				
				if(message != null) {
					String message_sender = message.getSender();
					if(!message_sender.equals(msg.Sender))
						Log.w(TAG, "Message sender, in json response, is not equals to content sender");
					
					values.put(MessageTable.COLUMN_SENDER, message_sender);
					values.put(MessageTable.COLUMN_RECIPIENT, message.getRecipient());
					values.put(MessageTable.COLUMN_CONTENT, message.getContent());
					// TODO: Check ContentType
					//values.put(MessageTable.COLUMN_CONTENT_TYPE, msg.ContentType.toString());
					//values.put(MessageTable.COLUMN_CONTENT_META, msg.ContentType.toString());
					
				} else {
					// TODO: Send notification to server, that the message is corrupt.
					// ....
					
					values.put(MessageTable.COLUMN_SENDER, msg.Sender);
					values.put(MessageTable.COLUMN_RECIPIENT, msg.Recipient);
					values.put(MessageTable.COLUMN_CONTENT, getString(R.string.message_status_cryptofail));
					values.put(MessageTable.COLUMN_CONTENT_TYPE, "TEXT");
					values.put(MessageTable.COLUMN_CONTENT_META, String.format("{\"encoding\":\"%s\"}", AppSettings.ENCODING));
				}
				
				insertOrUpdate(values);
			}
			return;
		}

		if(taskname.equals(HttpSendMessageTask.class.getName())) {
			Boolean success = (Boolean)result;
			if(!success)
				return;
				
			byte[] byte_input = (byte[])input;
			IMessage message = null;
			try {
				message = AppSettings.getCrypto().read_message(byte_input);
			} catch (MessageProtocolException ex) {
				Log.e(TAG, "FATAL: Can't read own message after sending.", ex);
				return;
			}
			
			String raw_content = Base64.encodeToString(byte_input, Base64.DEFAULT);
			ContentValues values = new ContentValues();
			values.put(MessageTable.COLUMN_KEY, (Long)null);
			values.put(MessageTable.COLUMN_SENDER, message.getSender());
			values.put(MessageTable.COLUMN_RECIPIENT, message.getRecipient());
			values.put(MessageTable.COLUMN_DATE, _dateFormat.format(new java.util.Date()));
			values.put(MessageTable.COLUMN_CONTENT, message.getContent());
			values.put(MessageTable.COLUMN_CONTENT_RAW, raw_content);
			// TODO: Check ContentType
			//values.put(MessageTable.COLUMN_CONTENT_TYPE, msg.ContentType.toString());
			//values.put(MessageTable.COLUMN_CONTENT_META, msg.ContentType.toString());
			
			insert(values);
			return;
		}
	}
	
	/**
	 * Adds the passed {@link ContentValues} to the {@link MessageContentProvider}.
	 * This methods decides if a update is need.
	 * 
	 * @param values Values to add as a row.
	 */
	private void insertOrUpdate(ContentValues values) {
		// Check if key is present and already compete.
		String key = values.getAsLong(MessageTable.COLUMN_KEY).toString();
		if(isKeyPresent(key))
			return;
		
		// Check if own message. This kind of message should be already available.
		if(AppSettings.getPhoneNumber().equalsHash(values.getAsString(MessageTable.COLUMN_SENDER))) {
			String where = String.format("%s IS NULL AND %s = ?", 
					MessageTable.COLUMN_KEY, 
					MessageTable.COLUMN_CONTENT_RAW);
			String[] whereArgs = new String[] { values.getAsString(MessageTable.COLUMN_CONTENT_RAW) };
			if(update(values, where, whereArgs) == 0) {
				insert(values);
			}				
		} else {
			insert(values);
		}
	}

	private boolean isKeyPresent(String key) {
		Cursor cursor = null; 
		try {
			cursor = getContentResolver().query(
				Uri.parse(MessageContentProvider.CONTENT_URI_KEY + "/" + key),
				null, null, null, null);
			return cursor != null && cursor.getCount() > 0;			
		} finally {
			if(cursor != null) cursor.close();
		}
	}
	
	/**
	 * Adds the passed {@link ContentValues} to the {@link MessageContentProvider}.
	 * @param values Values to add as a row.
	 */
	private void insert(ContentValues values) {
		getContentResolver().insert(
				MessageContentProvider.CONTENT_URI, 
				values);
	}
	
	private int update(ContentValues values, String where, String[] whereArgs) {		
		return getContentResolver().update(
			MessageContentProvider.CONTENT_URI, 
			values,
			where,
			whereArgs);
	}
	
	public class MessageBinder extends Binder {
		public MessageService getService() {
			return MessageService.this;
		}
	}
	
	private class MessageReceiver extends BroadcastReceiver {
		private final static String TAG = "MessageReceiver";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Message received.");
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
				if(receiver.getContact().equals(sender)) {
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

	private class MessageGetTask extends AsyncTask<MessageRequest, Void, MessageResponse[]> {
		protected ITaskPostListener _callback;
		private MessageRequest _input;
		private final Gson _gson;
		
		public MessageGetTask() {
			_gson = new GsonBuilder()
				.setDateFormat(ISO8601DateFormat.PATTERN)
				.create();
		}

		@Override
		protected MessageResponse[] doInBackground(MessageRequest... requests) {
			MessageResponse[] result = null;
			if(requests.length <= 0)
				return result;
			
			_input = requests[0];
			AndroidHttpClient client = AndroidHttpClient.newInstance(AppSettings.HTTP.USER_AGENT);
			try {
		        HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.GET_MESSAGE);
				post.addHeader("Content-Type", 	"application/json; charset=utf-8");
				post.addHeader("Accept",		"application/json; charset=utf-8");
				post.addHeader("Accept-Charset","utf-8");
		        
				// Request One!
				long[] ids = doRequestOne(client, post);
	            if(ids == null) return result;
	            
	            // Make Array to List
				Long[] longObjects = ArrayUtils.toObject(ids);
				List<Long> longList = java.util.Arrays.asList(longObjects);
	            
	            // Filter ids and check which ids are unknown.
	            Cursor match_cursor = getContentResolver().query(MessageContentProvider.CONTENT_URI,
	            	new String[] { MessageTable.COLUMN_KEY },
	            	buildSelection(ids.length), 
	            	buildSelectionWhere(longList),
	            	null);
	            
	            ArrayList<Long> request_ids = new ArrayList<Long>();
	            request_ids.addAll(longList);
	            while(match_cursor.moveToNext()) {
	            	int index_key = match_cursor.getColumnIndex(MessageTable.COLUMN_KEY);
	            	long known_id = Long.parseLong(match_cursor.getString(index_key));

	            	if(longList.contains(known_id))
	            		request_ids.remove(known_id);
	            }
	            
	            // Request Two, if first was successful.
	            result = doRequestTwo(client, post, request_ids);
			} catch(UnsupportedEncodingException ex) {
				Log.e(TAG, "UnsupportedEncodingException", ex);
			} catch (ClientProtocolException ex) {
				Log.e(TAG, "ClientProtocolException", ex);
			} catch (IOException ex) {
				Log.e(TAG, "IOException", ex);
			} finally {
				client.close();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(MessageResponse[] result) {
			super.onPostExecute(result);
			if(_callback != null) 
				_callback.onPostExecute(MessageGetTask.class.getName(), _input, result);
		}
		
		private long[] doRequestOne(HttpClient client, HttpPost post) throws ClientProtocolException, IOException {
			String request_string = _gson.toJson(_input.getRequestOne());
			StringEntity entity = new StringEntity(request_string);
        	post.setEntity(entity);
            HttpResponse response = client.execute(post);
            
            StatusLine statusLine = response.getStatusLine();
	        if(statusLine.getStatusCode() == java.net.HttpURLConnection.HTTP_OK){
	            byte[] result_bytes = EntityUtils.toByteArray(response.getEntity());
	            String result_string = new String(result_bytes, "UTF-8");
	            return _gson.fromJson(result_string, long[].class);
	        }
	        
	        return null;
		}
		
		private MessageResponse[] doRequestTwo(HttpClient client, HttpPost post, List<Long> ids) throws ClientProtocolException, IOException {
			String request_string = _gson.toJson(_input.getRequestTwo(ids));
			StringEntity entity = new StringEntity(request_string);
        	post.setEntity(entity);
            HttpResponse response = client.execute(post);
            
            StatusLine statusLine = response.getStatusLine();
	        if(statusLine.getStatusCode() == java.net.HttpURLConnection.HTTP_OK){
	            byte[] result_bytes = EntityUtils.toByteArray(response.getEntity());
	            String result_string = new String(result_bytes, "UTF-8");
	            return _gson.fromJson(result_string, MessageResponse[].class);
	        }
	        
	        return null;
		}
		
		private String buildSelection(int count) {
			ArrayList<String> list = new ArrayList<String>(count);
			for(int i = 0; i < count; i++) {
				list.add(MessageTable.COLUMN_KEY + " = ?");
			}
			return StringUtils.join(list, " OR ");
		}
		
		private String[] buildSelectionWhere(List<Long> ids) {
			List<String> transformed = Lists.transform(ids, Functions.toStringFunction());
			return transformed.toArray(new String[transformed.size()]);
		}
		
		public MessageGetTask setCallback(ITaskPostListener callback) {
			_callback = callback;
			return this;
		}
	}

	
}
