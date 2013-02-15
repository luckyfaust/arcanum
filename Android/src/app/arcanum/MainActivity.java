package app.arcanum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import app.arcanum.contracts.RegisterRequest;
import app.arcanum.tasks.HttpSendRegisterTask;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private final LayoutParams msg_layout_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private String regId = "";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        // Init AppSettings
        AppSettings.init(this);
        
        try {
        	// Init App
        	registerClient();
        	registerClientAtServer();
		} catch (InterruptedException ex) {
			Log.wtf(TAG, "Fatal error while register the current client.", ex);
		}
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Intent myIntent = new Intent(getBaseContext(), MessageActivity.class);
        startActivity(myIntent);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
	@Override
	protected void onDestroy() {
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}

	private void registerClient() {
		addProcessLine(Log.INFO, "Start registration.");
		
		try {
			// Check that the device supports GCM.
			GCMRegistrar.checkDevice(this);
			// Check the manifest permissions.
			GCMRegistrar.checkManifest(this);
			
			// Get the existing registration id, if it exists.
			regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				addProcessLine(Log.INFO, "GCM registration not found!");
				// Do the registration
				GCMRegistrar.register(this, AppSettings.GCM.PROJECT_ID);
				regId = GCMRegistrar.getRegistrationId(this);
				addProcessLine(Log.INFO, "GCM registration completed."); 
			} else {
				addProcessLine(Log.DEBUG, "GCM already registered.");
			}
		} catch (Exception ex) {
			addProcessLine(Log.ERROR, "Error while registration GCM.", ex);
		}
		
		//TODO: Remove this in the future!
		addProcessLine(Log.INFO, "Registration id = " + regId);
		AppSettings.GCM.REGISTRATION_ID = regId;
    }
 
    private void registerClientAtServer() throws InterruptedException {
    	addProcessLine(Log.INFO, "Start server registration.");
    	/*
    	addProcessLine(Log.INFO, "Waiting for key generation.");
    	if(!AppSettings.getCrypto().RSA.isReady())
    		AppSettings.getCrypto().RSA.waitForReady();
    	*/
    	addProcessLine(Log.DEBUG, "Creating RegisterRequest.");
    	RegisterRequest req = new RegisterRequest(this);
    	addProcessLine(Log.DEBUG, "RegisterRequest creation finished.");
    	
    	addProcessLine(Log.INFO, "Sending registration to server.");
    	HttpSendRegisterTask task = new HttpSendRegisterTask();
    	task.execute(req);
    }
    
    private void addProcessLine(int error, String string) {
    	addProcessLine(error, string, null);
    }
    
    private void addProcessLine(int error, String message, Throwable ex) {
    	switch (error) {
	    	case Log.VERBOSE:
				Log.v(TAG, message, ex);
				break;
			case Log.DEBUG:
				Log.d(TAG, message, ex);
				break;
			case Log.INFO:
				Log.i(TAG, message, ex);
				break;
			case Log.WARN:
				Log.w(TAG, message, ex);
				break;
			case Log.ERROR:
				Log.e(TAG, message, ex);
				break;
		}
    	
		final EditText line = new EditText(this);
		line.setLayoutParams(msg_layout_params);
		line.setEms(10);
		line.setText(message);
		line.setEnabled(false);
		
		final LinearLayout listOfProcess = (LinearLayout)findViewById(R.id.mainListProcess);
		listOfProcess.addView(line);
    }
}
