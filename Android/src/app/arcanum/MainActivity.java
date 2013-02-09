package app.arcanum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private final LayoutParams process_layout_params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	private String regId = "";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Init AppSettings
        AppSettings.init(this);
        
        // Init App
        registerClient();
        registerClientAtServer();
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
    }
 
    private void registerClientAtServer() {
    	addProcessLine(Log.INFO, "Start server registration.");
    	//TODO: Server registration! 
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
    	
		TextView line = new TextView(this);
		line.setLayoutParams(process_layout_params);
		line.setText(message);
		
		final LinearLayout listOfProcess = (LinearLayout)findViewById(R.id.mainListProcess);
		listOfProcess.addView(line);
    }
}
