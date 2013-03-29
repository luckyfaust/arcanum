package app.arcanum.ui.adapters;


import java.text.DateFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.arcanum.AppSettings;
import app.arcanum.ui.contracts.MessageItem;
import app.arcanum.ui.contracts.TextMessageItem;

public final class MessageViewAdapter extends ArrayAdapter<MessageItem> {
	private final List<MessageItem> _items;
	
	public MessageViewAdapter(Context context, int textViewResourceId, List<MessageItem> objects) {
		super(context, textViewResourceId, objects);
		_items = objects;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(app.arcanum.R.layout.custom_listview_message_row, null);
        }

        MessageItem item = _items.get(position);
        switch(item.getContentType()) {
        	case TEXT:
        		if(!(item instanceof TextMessageItem)) {
        			showDefault(v, item); 
            		break;
        		}
        		showText(v, (TextMessageItem)item);
			case IMAGE:
			case GEO:
			case VIDEO:
			default:
				showDefault(v, item);
				break;        
        }
        return v;
	}

	private void showText(View view, TextMessageItem item) {
		if (item == null)
			return;
		
		// Set margin to show own messages
		View container = view.findViewById(app.arcanum.R.id.message_listitem_container);
		LinearLayout.LayoutParams containerLayout = (LinearLayout.LayoutParams)container.getLayoutParams();
		if(containerLayout == null) {
			containerLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			container.setLayoutParams(containerLayout);
		}
		boolean isOwn = item.getSender().equals(AppSettings.getPhoneNumber().getHash());
		
		
		
		int margin = getMargin();		
		containerLayout.setMargins(
			isOwn ? margin : 5,
			0,
			isOwn ? 5 : margin,
			0
		);
		
		// Set view content
        TextView content = (TextView)view.findViewById(app.arcanum.R.id.message_listitem_content);
	    if (content != null) {
	    	content.setText(item.getContent());
	    }
	    
	    TextView timestamp = (TextView)view.findViewById(app.arcanum.R.id.message_listitem_timestamp);
	    if (timestamp != null) {
	    	timestamp.setText(DateFormat.getDateInstance(DateFormat.LONG).format(item.getTimestamp()));
	    }
	}

	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private int getMargin() {
		WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
		if(android.os.Build.VERSION.SDK_INT >= 13) {
			android.graphics.Point p = new android.graphics.Point();
			display.getSize(p);
		} else { 
			return display.getWidth(); 			 
		}
		return 0;
	}

	private void showDefault(View v, MessageItem item) {
		
	}

	public List<MessageItem> getDataSource() {
		return _items;
	}	
}
