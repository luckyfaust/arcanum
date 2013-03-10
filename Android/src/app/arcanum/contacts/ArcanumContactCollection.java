package app.arcanum.contacts;

import java.util.ArrayList;
import java.util.Collection;

public class ArcanumContactCollection extends ArrayList<ArcanumContact> {
	private static final long serialVersionUID = 2763479958883524750L;
	
	public ArcanumContactCollection() { super(); }
	public ArcanumContactCollection(Collection<ArcanumContact> values) { super(values);	}
	
	public ArcanumContact first() {
		if(size() > 0)
			return get(0);
		return null;
	}
}
