package app.arcanum.helper;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
	public static void showShort(Context context, String text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public static void showShort(Context context, int resid, Object... args) {
		showShort(context, context.getString(resid, args));
	}
	
	public static void showLong(Context context, String text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static void showLong(Context context, int resid, Object... args) {
		showLong(context, context.getString(resid, args));
	}
}
