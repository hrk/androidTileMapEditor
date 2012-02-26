package it.sineo.android.tileMapEditor;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAdapter {
	private final static String TAG = DatabaseAdapter.class.getSimpleName();

	/*
	 * Public constants
	 */
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "_name";
	public static final String KEY_JSON_DATA = "_json_data";
	public static final String KEY_LAST_UPDATE = "_last_update";

	/*
	 * Private database fields and constants
	 */
	private static final String DATABASE_NAME = "TileMapEditor";
	private static final String DATABASE_TABLE = "t_map";
	private static final int DATABASE_VERSION = 1;

	/*
	 * Helper class handling database creation and upgrade.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		Context context;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(context.getString(R.string.sql_ddl_create));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Nothing to do.
		}
	}

	/*
	 * 
	 */
	private final Context context;
	private DatabaseHelper helper;
	private SQLiteDatabase db;

	public DatabaseAdapter(Context ctx) {
		this.context = ctx;
		helper = new DatabaseHelper(context);
	}

	public DatabaseAdapter open() throws SQLException {
		db = helper.getWritableDatabase();
		return this;
	}

	public void close() {
		helper.close();
	}

	/*
	 * CRUD operations
	 */
	public long insertMap(TileMap map) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, map.name);
		initialValues.put(KEY_JSON_DATA, map.toJSON());
		initialValues.put(KEY_LAST_UPDATE, System.currentTimeMillis());
		long id = db.insert(DATABASE_TABLE, null, initialValues);
		// map.id = id;
		int count = this.updateMap(id, map);
		if (count != 1) {
			Log.e(TAG, "update count != 1: " + count);
		}
		
		return id;
	}

	public int updateMap(long id, TileMap map) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, map.name);
		initialValues.put(KEY_JSON_DATA, map.toJSON());
		initialValues.put(KEY_LAST_UPDATE, System.currentTimeMillis());
		int count = db.update(DATABASE_TABLE, initialValues, KEY_ROWID + "=?", new String[] {
			"" + id
		});
		return count;
	}

	public void removeMap(long id) {
		String[] whereArgs = {
			Long.toString(id)
		};
		db.delete(DATABASE_TABLE, KEY_ROWID + "=?", whereArgs);
		/* Thumbnail */
		File thumbFile = new File(Util.getExternalThumbnailsDirectory(), "tn_" + id + ".png");
		if (thumbFile.exists()) {
			thumbFile.delete();
		}
	}

	public Cursor getAllMaps() {
		return db.query(DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_NAME, KEY_JSON_DATA, KEY_LAST_UPDATE
		}, null, null, null, null, KEY_LAST_UPDATE + " DESC");
	}
}
