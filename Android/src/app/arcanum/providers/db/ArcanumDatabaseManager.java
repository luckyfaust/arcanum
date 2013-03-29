package app.arcanum.providers.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import app.arcanum.providers.db.tables.MessageTable;

public class ArcanumDatabaseManager extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arcanum.db";
	private static final int DATABASE_VERSION = 2;

	public ArcanumDatabaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		MessageTable.onCreate(database);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		MessageTable.onUpgrade(database, oldVersion, newVersion);
	}
}
