package app.arcanum.ui.adapters;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import app.arcanum.contacts.ArcanumContact;

public class ArcanumContactAdapter extends ArrayAdapter<ArcanumContact>{
	private final ArrayList<ArcanumContact> _contacts;

	public ArcanumContactAdapter(Context context, int textViewResourceId, ArrayList<ArcanumContact> contacts) {
		super(context, textViewResourceId, contacts);
		_contacts = contacts;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(app.arcanum.R.layout.custom_listview_contact_row, null);
        }

        ArcanumContact contact = _contacts.get(position);
        if (contact != null) {
            TextView name = (TextView)v.findViewById(app.arcanum.R.id.contact_listitem_name);
            TextView phone = (TextView)v.findViewById(app.arcanum.R.id.contact_listitem_phone);
	        if (name != null) {
	            name.setText(contact.DisplayName);
	        }

	        if(phone != null) {
	            phone.setText(contact.PhoneNumbers.get(0).getPhone());
	        }
	    }
        return v;
    }
}
