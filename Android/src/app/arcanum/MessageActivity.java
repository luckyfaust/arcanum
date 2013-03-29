package app.arcanum;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.IMessageReceiver;
import app.arcanum.helper.ToastUtils;
import app.arcanum.services.MessageService;
import app.arcanum.tasks.contracts.MessageResponse;
import app.arcanum.ui.adapters.MessageViewAdapter;
import app.arcanum.ui.contracts.MessageItem;
import app.arcanum.ui.contracts.MessageItemCollection;
import app.arcanum.ui.contracts.SyncType;

public class MessageActivity extends Activity implements IMessageReceiver {
	private final ServiceConnection _messageServiceConnection = new MessageServiceConnection();
	
	private ArcanumContact _contact;
	private MessageService _messageService;
	
	private ListView 				_messageView;
	private MessageViewAdapter 		_messageViewAdapter;
	private MessageItemCollection 	_messageViewList;

	private String TAG;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		if(!AppSettings.isInitialized)
			AppSettings.init(this);
		
		// Initialize contacts
		MessageClickListener clickListener = new MessageClickListener();
		_messageViewList = new MessageItemCollection();
		_messageViewAdapter = new MessageViewAdapter(this, android.R.layout.simple_list_item_1, _messageViewList);
		_messageView = (ListView)findViewById(R.id.message_listview);
		_messageView.setAdapter(_messageViewAdapter);
		_messageView.setOnItemClickListener(clickListener);
		_messageView.setOnItemLongClickListener(clickListener);
		
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
        
        // Set contact & Activity title
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	Object contact = bundle.get("contact");
            if(contact != null && contact instanceof ArcanumContact) {
            	_contact = (ArcanumContact)contact;
            	setTitle(getString(R.string.msg_title, _contact.DisplayName));
            }
        }  
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
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void syncMessages(SyncType requestType) {
		if(_messageService == null || _contact == null)
			return;
		
		MessageResponse[] result;
		switch(requestType) {
			case PUSHED:
				result = _messageService.getUnreadMessages(_contact);
				break;
			case INIT:
			default:
				result = _messageService.getMessages(_contact);
				break;			
		}
		
		if(result == null || result.length == 0)
			return;
		
		for(MessageResponse item : result) {
			if(_messageViewList.containsByID(item.Key))
				continue;
			Log.d(TAG, String.format("Adding message item with id %d.", item.Key));
			
			MessageItem newItem = MessageItem.newInstance(item);
			if(newItem.setContent(item.Content))
				_messageViewList.add(newItem);
		}
		_messageViewAdapter.notifyDataSetChanged();
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
			//_messageService = null;
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
}
