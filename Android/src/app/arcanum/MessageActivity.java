package app.arcanum;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.IMessageReceiver;
import app.arcanum.crypto.SHA256;
import app.arcanum.helper.ToastUtils;
import app.arcanum.providers.MessageContentProvider;
import app.arcanum.providers.db.tables.MessageTable;
import app.arcanum.services.MessageService;
import app.arcanum.ui.adapters.MessageViewAdapter;
import app.arcanum.ui.contracts.SyncType;

public class MessageActivity extends FragmentActivity implements IMessageReceiver, LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = MessageActivity.class.getSimpleName();
	private static final String[] PROJECTION = new String[] { 
		MessageTable.COLUMN_ID,
		MessageTable.COLUMN_SENDER,
		MessageTable.COLUMN_RECIPIENT,
		MessageTable.COLUMN_CONTENT,
		MessageTable.COLUMN_DATE
	};
	private static final String SELECTION = MessageTable.COLUMN_SENDER + " = ? OR " + MessageTable.COLUMN_RECIPIENT + " = ?";
	private static final String SORT_ORDER = MessageTable.COLUMN_DATE + " COLLATE LOCALIZED ASC";
	private static final int LOADER_ID = 1;
	
	private final ServiceConnection _messageServiceConnection = new MessageServiceConnection();
	
	private ArcanumContact _contact;
	private MessageService _messageService;
	
	private ListView 			_messageView;
	private MessageViewAdapter 	_messageViewAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		if(!AppSettings.isInitialized)
			AppSettings.init(this);
		
		// Set contact & Activity title
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	Object contact = bundle.get("contact");
            if(contact != null && contact instanceof ArcanumContact) {
            	_contact = (ArcanumContact)contact;
            	setTitle(getString(R.string.msg_title, _contact.DisplayName));
            }
        }  
                
        // Initialize Content Provider connection with ListView
		MessageClickListener clickListener = new MessageClickListener();		
		_messageViewAdapter = new MessageViewAdapter(this);
		_messageView = (ListView)findViewById(R.id.message_listview);
		_messageView.setAdapter(_messageViewAdapter);
		_messageView.setOnItemClickListener(clickListener);
		_messageView.setOnItemLongClickListener(clickListener);
		
		LoaderManager lm = getSupportLoaderManager();
	    lm.initLoader(LOADER_ID, null, this);
		
		// Get Controls
        final EditText txtMessage = (EditText)findViewById(R.id.message_edittext);
        final Button button = (Button) findViewById(R.id.message_send);
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	final String sendText = txtMessage.getText().toString();
				// TODO: Dummy add current send message.
				// final EditText myMessage = build_MessageControl(sendText, true);
            	// listOfMessages.addView(myMessage);
				
				if(_messageService != null && _messageService.sendMessage(_contact, sendText)) {
					ToastUtils.showShort(MessageActivity.this, R.string.toast_msg_send_success);
					txtMessage.setText(null);
				} else {
					ToastUtils.showShort(MessageActivity.this, R.string.toast_msg_send_failed);				
				}
            }
        });
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// Bind to MessageService
        Intent messageServiceIntent = new Intent(this, MessageService.class);
        bindService(messageServiceIntent, _messageServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// Unbind to MessageService
		unbindService(_messageServiceConnection);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(_messageService == null)
			return;
		_messageService.registerMessageActivity(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(_messageService == null)
			return;
		_messageService.unregisterMessageActivity(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_message, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.menu_load_server_key:
		    	//_arcanumCrypto.get_rsa().load_serverPublicKey();
		        return true;
		    case R.id.menu_contacts:
		    	Intent myIntent = new Intent(getBaseContext(), ContactsActivity.class);
                startActivity(myIntent);
		    	return true;
		    case R.id.menu_settings:
				Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(settingsIntent);
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

	private void syncMessages(SyncType requestType) {
		if(_messageService == null || _contact == null)
			return;
		
		switch(requestType) {
			case PUSHED:
				_messageService.getUnreadMessages(_contact);
				break;
			case INIT:
			default:
				_messageService.getMessages(_contact);
				break;			
		}
	}

	private class MessageServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			if(!(binder instanceof MessageService.MessageBinder))
				return;
			_messageService = ((MessageService.MessageBinder)binder).getService();
			_messageService.registerMessageActivity(MessageActivity.this);

			// New binded, init history.
			syncMessages(SyncType.INIT);
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			_messageService.unregisterMessageActivity(MessageActivity.this);
			_messageService = null;
		}
	}

	private class MessageClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Object item = _messageView.getItemAtPosition(position);
			if(item != null && item instanceof ArcanumContact) {
				ArcanumContact contact = (ArcanumContact)item;
				Intent intent = new Intent(getBaseContext(), MessageActivity.class);
				intent.putExtra("contact", contact);
				startActivity(intent);
				finish();
			}
		}
		
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Object item = _messageView.getItemAtPosition(position);
			if(item == null || !(item instanceof ArcanumContact))
				return false;
			
			ArcanumContact contact = (ArcanumContact)item;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.withAppendedPath(android.provider.ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.LookupKey);
			intent.setData(uri);
			startActivity(intent);
			return true;
		}
	}
	
	@Override
	public ArcanumContact getContact() {
		return _contact;
	}

	@Override
	public void pushMessage() {
		syncMessages(SyncType.PUSHED);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch(id) {
			case LOADER_ID:
				String contactHash = SHA256.hash(_contact.Token);
				return new CursorLoader(this, MessageContentProvider.CONTENT_URI,
			        PROJECTION,
			        SELECTION,
			        new String[] { contactHash, contactHash },
			        SORT_ORDER);
		}
		Log.e(TAG, String.format("Creating Loader with id %d failed.", id));
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()) {
			case LOADER_ID:
				_messageViewAdapter.swapCursor(cursor);
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		_messageViewAdapter.swapCursor(null);
	}
}
