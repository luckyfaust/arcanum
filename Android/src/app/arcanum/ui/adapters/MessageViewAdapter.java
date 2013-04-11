package app.arcanum.ui.adapters;


import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.arcanum.AppSettings;
import app.arcanum.contracts.ISO8601DateFormat;
import app.arcanum.providers.db.tables.MessageTable;

public final class MessageViewAdapter extends CursorAdapter {
	private static final String TAG = MessageViewAdapter.class.getSimpleName();
	private static final DateFormat _databaseDateFormat = new ISO8601DateFormat();
		
	public MessageViewAdapter(Context context) {
		super(context, null, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// Create view
		View view = LayoutInflater.from(context).inflate(app.arcanum.R.layout.custom_listview_message_row, null);
		
		// Set margin to show own messages
 		View container = view.findViewById(app.arcanum.R.id.message_listitem_container);
 		LinearLayout.LayoutParams containerLayout = (LinearLayout.LayoutParams)container.getLayoutParams();
 		if(containerLayout == null) {
 			containerLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
 			container.setLayoutParams(containerLayout);
 		}
 		
 		final int index_sender = cursor.getColumnIndex(MessageTable.COLUMN_SENDER);
 		final String    sender = cursor.getString(index_sender);
 		
 		boolean isOwn = sender.equals(AppSettings.getPhoneNumber().getHash());
 		int margin = getMargin(context);
 		containerLayout.setMargins(
 			isOwn ? margin : 5,
 			0,
 			isOwn ? 5 : margin,
 			0
 		);
		
 		// Return correct view.
		return view;
	}
		
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Set content
        TextView contentView = (TextView)view.findViewById(app.arcanum.R.id.message_listitem_content);
	    if (contentView != null) {
	    	final int index_content = cursor.getColumnIndex(MessageTable.COLUMN_CONTENT);
	    	final byte[]    content = cursor.getBlob(index_content);
	    	
	    	contentView.setText(new String(content, AppSettings.ENCODING));
	    }
		
	    // Set date
	    TextView timestamp = (TextView)view.findViewById(app.arcanum.R.id.message_listitem_timestamp);
	    if (timestamp != null) {
	    	final int     index_date = cursor.getColumnIndex(MessageTable.COLUMN_DATE);
	    	
	    	String string_date = cursor.getString(index_date);
	    	try {
				Date date = _databaseDateFormat.parse(string_date);
				string_date = DateFormat.getDateInstance(DateFormat.LONG).format(date);
			} catch (ParseException ex) {
				Log.w(TAG, "Parsing date failed.", ex);
			}
	    	
			timestamp.setText(string_date);
	    }
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private int getMargin(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.graphics.Point p = new android.graphics.Point();
			display.getSize(p);
		} else { 
			return display.getWidth(); 			 
		}
		return 0;
	}	
}
