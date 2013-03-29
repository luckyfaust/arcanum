package app.arcanum.contracts;

import android.annotation.SuppressLint;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ISO8601DateFormat extends DateFormat {
	public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat _format = new SimpleDateFormat(PATTERN);
	private static final long serialVersionUID = -4623289588368356725L;

	@Override
	public StringBuffer format(Date date, StringBuffer buffer, FieldPosition field) {
		return _format.format(date, buffer, field);
	}

	@Override
	public Date parse(String string, ParsePosition position) {
		return _format.parse(string, position);
	} 
}
