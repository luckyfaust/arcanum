package app.arcanum.providers.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class MessageTable {
	// Database table
	public static final String TABLE_NAME = "message";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_SENDER = "sender";
	public static final String COLUMN_RECIPIENT = "recipient";
	public static final String COLUMN_DATE = "timestamp";
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_CONTENT_TYPE = "content_type";
	public static final String COLUMN_CONTENT_META = "content_metadata";
	public static final String COLUMN_CONTENT_RAW = "content_raw";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = String.format("CREATE TABLE %s (%s,%s,%s,%s,%s,%s,%s,%s,%s)", 
		TABLE_NAME,
		COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL",
		COLUMN_KEY + " BIGINT UNIQUE",
		COLUMN_SENDER + " TEXT NOT NULL",
		COLUMN_RECIPIENT + " TEXT NOT NULL",
		COLUMN_DATE + " TEXT NOT NULL",
		COLUMN_CONTENT + " TEXT NOT NULL",
		COLUMN_CONTENT_TYPE + " TEXT NOT NULL DEFAULT 'TEXT'",
		COLUMN_CONTENT_META + " TEXT",
		COLUMN_CONTENT_RAW + " TEXT NOT NULL"
		);
	public static final String[] ALL_COLUMNS = new String[] {
		COLUMN_ID, COLUMN_KEY, COLUMN_SENDER, 
		COLUMN_RECIPIENT, COLUMN_DATE, COLUMN_CONTENT, 
		COLUMN_CONTENT_TYPE, COLUMN_CONTENT_META, COLUMN_CONTENT_RAW
	};

	public static void onCreate(SQLiteDatabase database) {
		Log.i(MessageTable.class.getName(), "Creating Table with following statement:\n" + DATABASE_CREATE);
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.i(MessageTable.class.getName(), String.format("Upgrading database from version %d to %d, which will destroy all old data.", oldVersion, newVersion));
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		
		onCreate(database);
	}
}
