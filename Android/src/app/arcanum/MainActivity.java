package app.arcanum;

import java.nio.charset.Charset;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import app.arcanum.contacts.ArcanumContact;
import app.arcanum.crypto.ArcanumCrypto;
import app.arcanum.crypto.exceptions.MessageProtocolException;

public class MainActivity extends Activity {
	private ArcanumCrypto _arcanumCrypto;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        _arcanumCrypto = new ArcanumCrypto();
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Button btnSend = (Button)findViewById(R.id.btnSend);
        final EditText txtMessage = (EditText)findViewById(R.id.txtMessage);
        btnSend.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				builder
					.setTitle("Attention")
					.setMessage(String.format("You send: \"%1$s\"", txtMessage.getText()))
					.setPositiveButton("OK", dialogClickListener)
				    .show();
				
				try {
					ArcanumContact to 	= new ArcanumContact();
					to.Name 			= "Test User";
					to.RawContactID 	= 42;
					to.Token			= "+49 175 12 34 567";
										
					byte[] sendBytes 	= _arcanumCrypto.create_message(to, txtMessage.getText().toString());
					String sendString 	= Base64.encodeToString(sendBytes, Base64.DEFAULT);
					
					builder
						.setTitle("Attention")
						.setMessage(String.format("You will send: \"%1$s\"", sendString))
						.setPositiveButton("OK", dialogClickListener)
					    .show();
				} catch (MessageProtocolException ex) {
					builder
						.setTitle("ERROR")
						.setMessage(String.format("ERROR:\n%1$s", ex.getMessage()))
						.setPositiveButton("OK", dialogClickListener)
					    .show();
				}				
			}
		});
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.menu_load_server_key:
		    	_arcanumCrypto.get_rsa().load_serverPublicKey();
		        return true;
		    case R.id.menu_contacts:
		    	setContentView(R.layout.activity_contacts);
		    	return true;
		    case R.id.menu_settings:
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
    
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
	        switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	            //Yes button clicked
	            break;

	        case DialogInterface.BUTTON_NEGATIVE:
	            //No button clicked
	            break;
	        }
	    }
	};
}
