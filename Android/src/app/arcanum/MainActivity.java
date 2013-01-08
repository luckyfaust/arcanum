package app.arcanum;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
