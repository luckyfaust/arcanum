package app.arcanum.providers;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import app.arcanum.providers.db.ArcanumDatabaseManager;
import app.arcanum.providers.db.tables.MessageTable;

public class MessageContentProvider extends ContentProvider {
	private static final String AUTHORITY = "app.arcanum.providers";
	private static final String BASE_PATH = "messages";
	// URI MATCHER
	private static final UriMatcher Matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int MESSAGES = 10;
	private static final int MESSAGE_ID = 20;
	private static final int MESSAGE_KEY = 30;
	  
	public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/%s", AUTHORITY, BASE_PATH));
	public static final Uri CONTENT_URI_KEY = Uri.parse(String.format("%s/key", CONTENT_URI));
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/messages";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/message";
	
	private ArcanumDatabaseManager _database;
	
	static {
		Matcher.addURI(AUTHORITY, BASE_PATH, MESSAGES);
		Matcher.addURI(AUTHORITY, BASE_PATH + "/#", MESSAGE_ID);
		Matcher.addURI(AUTHORITY, BASE_PATH + "/key/#", MESSAGE_KEY);
	}
	
	@Override
	public boolean onCreate() {
		_database = new ArcanumDatabaseManager(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		checkColumns(projection);
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(MessageTable.TABLE_NAME);
		
		// Filter query
		int uriType = Matcher.match(uri);
		switch (uriType) {
			case MESSAGES:
				break;
			case MESSAGE_ID:
				String where_id = getWhereClause(MessageTable.COLUMN_ID, uri.getLastPathSegment());
				builder.appendWhere(where_id);
				break;
			case MESSAGE_KEY:
				String where_key = getWhereClause(MessageTable.COLUMN_KEY, uri.getLastPathSegment());
				builder.appendWhere(where_key);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		SQLiteDatabase db = _database.getReadableDatabase();
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = 0L;
		
		SQLiteDatabase db = _database.getWritableDatabase();
		int uriType = Matcher.match(uri);
		switch (uriType) {
			case MESSAGES:
				id = db.insert(MessageTable.TABLE_NAME, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rowsDeleted = 0;
		
		SQLiteDatabase db = _database.getWritableDatabase();
		int uriType = Matcher.match(uri);
		switch (uriType) {
			case MESSAGES:
				rowsDeleted = db.delete(MessageTable.TABLE_NAME, selection, selectionArgs);
				break;
			case MESSAGE_ID:
				String where_id = getWhereClause(MessageTable.COLUMN_ID, uri.getLastPathSegment());
				if(StringUtils.isEmpty(selection)) {
					rowsDeleted = db.delete(MessageTable.TABLE_NAME, 
						where_id, 
						null);
				} else {
					String where_id_and_selection = getAndClause(where_id, selection);
					rowsDeleted = db.delete(MessageTable.TABLE_NAME, 
						where_id_and_selection, 
						selectionArgs);
				}
				break;
			case MESSAGE_KEY:
				String where_key = getWhereClause(MessageTable.COLUMN_KEY, uri.getLastPathSegment());
				if(StringUtils.isEmpty(selection)) {
					rowsDeleted = db.delete(MessageTable.TABLE_NAME, 
						where_key, 
						null);
				} else {
					String where_id_and_selection = getAndClause(where_key, selection);
					rowsDeleted = db.delete(MessageTable.TABLE_NAME, 
						where_id_and_selection, 
						selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowsUpdated = 0;
		
		SQLiteDatabase db = _database.getWritableDatabase();
		int uriType = Matcher.match(uri);
		switch (uriType) {
			case MESSAGES:
				rowsUpdated = db.update(MessageTable.TABLE_NAME, values, selection, selectionArgs);
				break;
			case MESSAGE_ID:
				String where_id = getWhereClause(MessageTable.COLUMN_ID, uri.getLastPathSegment());
				if(StringUtils.isEmpty(selection)) {
					rowsUpdated = db.update(MessageTable.TABLE_NAME,
						values,
						where_id, 
						null);
				} else {
					String where_id_and_selection = getAndClause(where_id, selection);
					rowsUpdated = db.update(MessageTable.TABLE_NAME,
						values,
						where_id_and_selection, 
						selectionArgs);
				}
				break;
			case MESSAGE_KEY:
				String where_key = getWhereClause(MessageTable.COLUMN_KEY, uri.getLastPathSegment());
				if(StringUtils.isEmpty(selection)) {
					rowsUpdated = db.update(MessageTable.TABLE_NAME,
							values,
							where_key,
							null);
				} else {
					String where_id_and_selection = getAndClause(where_key, selection);
					rowsUpdated = db.update(MessageTable.TABLE_NAME,
							values,
							where_id_and_selection, 
							selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		if (projection == null)
			return;
		
		HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
		HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(MessageTable.ALL_COLUMNS));
		//requestedColumns.retainAll(Arrays.asList()); // TODO: Why doesn't work?
		
		if(!availableColumns.containsAll(requestedColumns)) {
			throw new IllegalArgumentException(String.format("Unknown columns (%s) in projection.",
				StringUtils.join(requestedColumns, ',')));
		}
	}
	
	private static String getWhereClause(String column, String value) {
		return String.format("%s = %s", column, value); 
	}
	
	private static String getAndClause(String left_clause, String right_clause) {
		return String.format("%s AND %s", left_clause, right_clause); 
	}
}
