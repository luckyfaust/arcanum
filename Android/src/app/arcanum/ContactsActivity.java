package app.arcanum;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import app.arcanum.contacts.ArcanumContactManager;
import app.arcanum.contacts.PossibleContact;

public class ContactsActivity extends Activity {
	private ArcanumContactManager _manager;
	
	private final LayoutParams contract_layout_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private final LayoutParams contract_image_params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	private final LayoutParams contract_txt_params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		// Init Helpers
		_manager = new ArcanumContactManager(this.getContentResolver());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		refreshContactList();			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_contacts, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.menu_contacts_sync:
	    		_manager.Sync();
	    		refreshContactList();
	    		return true;
		    case R.id.menu_settings:
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void refreshContactList() {
		final LinearLayout layout_contacts = (LinearLayout)findViewById(R.id.layoutContacts);
		layout_contacts.removeAllViews();
		
		for(PossibleContact c : _manager.getAll()) {
			LinearLayout layout_item = build_ContactLine(String.format("%1$s\n%2$s", c.DisplayName, c.PhoneNumbers.get(0)));
			layout_contacts.addView(layout_item);
		}
	}

	private LinearLayout build_ContactLine(final String name) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(contract_layout_params);
		
		ImageView img = new ImageView(this);
		img.setLayoutParams(contract_image_params);
		img.setImageResource(R.drawable.ic_launcher);
		
		EditText txt = new EditText(this);
		txt.setLayoutParams(contract_txt_params);
		txt.setEms(10);
		txt.setText(name);
		txt.setEnabled(false);
				
		layout.addView(img);
		layout.addView(txt);
		
		return layout;
	}
}
