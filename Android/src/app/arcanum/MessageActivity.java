package app.arcanum;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.contracts.IMessageReceiver;
import app.arcanum.helper.ToastUtils;
import app.arcanum.services.MessageService;
import app.arcanum.tasks.contracts.MessageResponse;

public class MessageActivity extends Activity implements IMessageReceiver {
	private final LayoutParams msg_layout_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private final LayoutParams msg_layout_params_own = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	private final ServiceConnection _messageServiceConnection = new MessageServiceConnection();
	
	private ArcanumContact _contact;
	private MessageService _messageService;
	private MessageResponse[] _messagesRaw;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		msg_layout_params.setMargins(0, 0, 50, 0);
        msg_layout_params_own.setMargins(50, 0, 0, 0);
        
        // Get Controls
        final EditText txtMessage = (EditText)findViewById(R.id.txtMessage);
        final LinearLayout listOfMessages = (LinearLayout)findViewById(R.id.listMessages);
        final Button button = (Button) findViewById(R.id.btnSend);
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	final String sendText = txtMessage.getText().toString();
				final EditText myMessage = build_MessageControl(sendText, true);
				listOfMessages.addView(myMessage);
								
				ArcanumContact to 	= new ArcanumContact();
				to.Token			= AppSettings.getPhoneNumber();
									
				if(_messageService != null && _messageService.sendMessage(to, sendText)) {
					ToastUtils.showShort(MessageActivity.this, R.string.toast_msg_send_success);
					txtMessage.setText(null);
				} else {
					ToastUtils.showShort(MessageActivity.this, R.string.toast_msg_send_failed);				
				}
            }
        });
        
        // Bind to MessageService
        Intent messageServiceIntent = new Intent(this, MessageService.class);
        bindService(messageServiceIntent, _messageServiceConnection, Context.BIND_AUTO_CREATE);
        
        // Load History and set contact
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	Object contact = bundle.get("contact");
            if(contact != null && contact instanceof ArcanumContact) {
            	_contact = (ArcanumContact)contact;
            	setTitle(getString(R.string.msg_title, _contact.DisplayName));
            	syncMessages();
            }
        }  
	}
	
	@Override
	protected void onDestroy() {
		unbindService(_messageServiceConnection);
		super.onDestroy();
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
	
	private void syncMessages() {
		if(_messageService == null || _contact == null)
			return;
		
		_messagesRaw = _messageService.getMessages(_contact);
	}

	private EditText build_MessageControl(final String msg, final boolean isOwnMessage) {
		EditText txt = new EditText(this);
		txt.setLayoutParams(isOwnMessage ? msg_layout_params_own : msg_layout_params);
		txt.setEms(10);
		txt.setText(msg);
		txt.setEnabled(false);
				
		return txt;
	}
	
	private class MessageServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			if(!(binder instanceof MessageService.MessageBinder))
				return;
			_messageService = ((MessageService.MessageBinder)binder).getService();
			_messageService.registerMessageActivity(MessageActivity.this);
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			_messageService.unregisterMessageActivity(MessageActivity.this);
			_messageService = null;			
		}	
	}

	@Override
	public ArcanumContact getContact() {
		return _contact;
	}

	@Override
	public void pushMessage() {
		syncMessages();
	}
}
