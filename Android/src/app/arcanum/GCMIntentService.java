package app.arcanum;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import app.arcanum.contacts.ArcanumContactManager;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String TAG = "GCMIntentService";
    
    public GCMIntentService() {
        super(AppSettings.GCM.PROJECT_ID);
        Log.d(TAG, "GCMIntentService init");
    }
    
    @Override
	/**
	 * Called when your server sends a message to GCM, and GCM delivers it to the device. If the message has a payload, its contents are available as extras in the intent.
	 */
	protected void onMessage(Context context, Intent intent) {
    	Log.d(TAG, "Message Received");
    	String type = intent.getStringExtra("type");
    	String sender = intent.getStringExtra("sender");

    	//TODO: Load contact by sender hash.
    	int sender_id = 1;
        
    	// Creating a Notification
    	// http://developer.android.com/guide/topics/ui/notifiers/notifications.html#CreateNotification
    	Intent msgIntent = new Intent(this, MessageActivity.class);
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(MessageActivity.class);
    	stackBuilder.addNextIntent(msgIntent);
    	PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_launcher) // required
    	        .setContentTitle(GetContentTitleFromType(type)) // required
    	        .setContentText(GetContentTextFromType(type, sender)) // required
    	        .setContentIntent(msgPendingIntent)
    	        .setAutoCancel(true);
    	
    	NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.notify(type, sender_id, mBuilder.build());
	}
    
    private String GetContentTitleFromType(String type) {
    	if(type == "message")
    		return getString(R.string.notfiy_new_message);
    	return getString(R.string.app_name);
	}
    
    private String GetContentTextFromType(String type, String... additional) {
    	if(type == "message")
    		return getString(R.string.notfiy_new_message_detail);
    	return getString(R.string.app_name);
	}

	@Override
	/**
	 * Called after a registration intent is received, passes the registration ID assigned by GCM to that device/application pair as parameter. Typically, you should send the regid to your server so it can use it to send messages to this device.
	 */
	protected void onRegistered(Context context, String regId) {
		Log.d(TAG, regId);
		AppSettings.GCM.REGISTRATION_ID = regId;
	}
    
    @Override
	/**
	 * Called after the device has been unregistered from GCM. Typically, you should send the regid to the server so it unregisters the device.
	 */
	protected void onUnregistered(Context context, String regId) {
		Log.d(TAG, regId);
		AppSettings.GCM.REGISTRATION_ID = null;
	}
    
    /**
	 * Called when the device tries to register or unregister, but GCM returned an error. Typically, there is nothing to be done other than evaluating the error (returned by errorId) and trying to fix the problem.
	 */
	@Override
	protected void onError(Context context, String errorId) {
		Log.e(TAG, errorId);
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		super.onDeletedMessages(context, total);
		Log.d(TAG, "DeletedMessages");
	}

	@Override
	/**
	 * Called when the device tries to register or unregister, but the GCM servers are unavailable. The GCM library will retry the operation using exponential backup, unless this method is overridden and returns false. This method is optional and should be overridden only if you want to display the message to the user or cancel the retry attempts.
	 */
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}
	
	private void sendGCMIntent(Context context, String message) {        
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("GCM_RECEIVED_ACTION");
		broadcastIntent.putExtra("gcm", message);
		context.sendBroadcast(broadcastIntent);
    }
}
