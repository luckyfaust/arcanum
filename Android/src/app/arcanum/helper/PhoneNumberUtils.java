package app.arcanum.helper;

import java.util.regex.Pattern;

public class PhoneNumberUtils {
	private final static Pattern regex_cleanup = Pattern.compile("[^0-9\\+]");
	private final static Pattern regex_prefix = Pattern.compile("\\A(00)");
	
	public static String preparePhoneNumber(final String phone) {
		String clean_phone = regex_cleanup.matcher(phone).replaceAll("");
		clean_phone = regex_prefix.matcher(clean_phone).replaceFirst("+");
		return clean_phone;
	}
}
