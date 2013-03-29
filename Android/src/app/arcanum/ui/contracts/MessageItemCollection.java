package app.arcanum.ui.contracts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MessageItemCollection extends ArrayList<MessageItem> {
	private static final long serialVersionUID = -1108157472565406256L;
	private static Comparator<MessageItem> _timestampSorter = new java.util.Comparator<MessageItem>() {
		public int compare(MessageItem lhd, MessageItem rhd) {
	    	return lhd.getTimestamp().compareTo(rhd.getTimestamp());
	    }
	};
	
	@Override
	public boolean add(MessageItem object) {
		boolean result = super.add(object);
		sort();
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends MessageItem> collection) {
		boolean result = super.addAll(collection);
		sort();
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends MessageItem> collection) {
		boolean result = super.addAll(index, collection);
		sort();
		return result;
	}

	public boolean containsByID(long identifier) {
		// TODO: Performance improvement needed.
		for(MessageItem item : this) {
			if(item.getIdentifier() == identifier)
				return true;
		}
		return false;
	}
	
	public void sort() {
		Collections.sort(this, _timestampSorter);
	}
}