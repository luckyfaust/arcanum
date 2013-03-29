package app.arcanum.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import app.arcanum.GCMIntentService;

public class StartupIntentReceiver extends BroadcastReceiver {
	private static final String TAG = "StartupIntentReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Starting MessageService now.");
		Intent messageService = new Intent(context, MessageService.class);
		context.startService(messageService);
		
		Log.d(TAG, "Starting GCMIntentService now.");
		Intent gcmService = new Intent(context, GCMIntentService.class);
		context.startService(gcmService);
	}
}
