/*
 * Copyright (C) 2012 Luca Santarelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.sineo.android.tileMapEditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class TileMapContentProvider extends ContentProvider {
	private final static String TAG = TileMapContentProvider.class.getSimpleName();
	/*
	 * Private database fields
	 */
	private UriMatcher uriMatcher;
	private DatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(C.AUTHORITY_MAP, C.CONTENT_MAP_BASE, C.MATCH_MAPS);
		uriMatcher.addURI(C.AUTHORITY_MAP, C.CONTENT_MAP_BASE + "/#", C.MATCH_MAP_ID);

		dbHelper = new DatabaseHelper(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case C.MATCH_MAPS:
			case C.MATCH_MAP_ID:
				return C.CONTENT_TYPE_MAP;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int uriType = uriMatcher.match(uri);
		if (uriType == C.MATCH_MAPS || uriType == C.MATCH_MAP_ID) {
			String mapId = null;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			if (uriType == C.MATCH_MAP_ID) {
				/*
				 * Single map: use WHERE clause; ensure the given argument is a long
				 * through parse+toString
				 */
				mapId = Long.decode(uri.getLastPathSegment()).toString();
				where = TileMap.Columns.KEY_ROWID + "=?";
				whereArgs = new String[] {
					mapId
				};
			} else {
				/* Whole database */
				where = null;
				whereArgs = null;
				mapId = null;
			}
			int deleted = db.delete(C.DATABASE_TABLE, where, whereArgs);
			// db.close();
			/* Thumbnails */
			File thumbDir = Util.getExternalThumbnailsDirectory();
			if (mapId == null) {
				File[] thumbNails = thumbDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						return filename.startsWith("tn_");
					}
				});
				for (File thumbFile : thumbNails) {
					if (thumbFile.exists())
						thumbFile.delete();
				}
			} else {
				File thumbFile = new File(thumbDir, "tn_" + mapId + ".png");
				if (thumbFile.exists()) {
					thumbFile.delete();
				}
			}
			getContext().getContentResolver().notifyChange(uri, null);
			// getContext().getContentResolver().notifyChange(C.CONTENT_URI, null);
			return deleted;
		} else {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) == C.MATCH_MAPS) {
			ContentValues dbValues = new ContentValues();
			dbValues.put(TileMap.Columns.KEY_NAME, initialValues.getAsString(TileMap.Columns.KEY_NAME));
			dbValues.put(TileMap.Columns.KEY_JSON_DATA, initialValues.getAsString(TileMap.Columns.KEY_JSON_DATA));
			dbValues.put(TileMap.Columns.KEY_LAST_UPDATE, System.currentTimeMillis());

			SQLiteDatabase db = dbHelper.getWritableDatabase();
			long mapId = db.insert(C.DATABASE_TABLE, null, dbValues);
			Uri newUri = Uri.withAppendedPath(C.CONTENT_URI, Long.toString(mapId));
			/* Create the thumbnail on the filesystem */
			try {
				File thumbFile = new File(Util.getExternalThumbnailsDirectory(), "tn_" + mapId + ".png");
				FileOutputStream fos = new FileOutputStream(thumbFile);
				fos.write(initialValues.getAsByteArray(TileMap.Columns.KEY_THUMB));
				fos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			getContext().getContentResolver().notifyChange(uri, null);
			return newUri;
		} else {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues initialValues, String selection, String[] selectionArgs) {
		int uriType = uriMatcher.match(uri);
		if (uriType == C.MATCH_MAP_ID) {
			/* Ensure well-formedness of the URI while getting the id: */
			String mapId = Long.decode(uri.getLastPathSegment()).toString();

			SQLiteDatabase db = dbHelper.getWritableDatabase();

			ContentValues dbValues = new ContentValues();
			dbValues.put(TileMap.Columns.KEY_NAME, initialValues.getAsString(TileMap.Columns.KEY_NAME));
			dbValues.put(TileMap.Columns.KEY_JSON_DATA, initialValues.getAsString(TileMap.Columns.KEY_JSON_DATA));
			dbValues.put(TileMap.Columns.KEY_LAST_UPDATE, System.currentTimeMillis());

			selection = TileMap.Columns.KEY_ROWID + "=?";
			selectionArgs = new String[] {
				mapId
			};

			int updated = db.update(C.DATABASE_TABLE, dbValues, selection, selectionArgs);

			/* Create the thumbnail on the filesystem */
			try {
				File thumbFile = new File(Util.getExternalThumbnailsDirectory(), "tn_" + mapId + ".png");
				FileOutputStream fos = new FileOutputStream(thumbFile);
				fos.write(initialValues.getAsByteArray(TileMap.Columns.KEY_THUMB));
				fos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			/* Notify observers */
			getContext().getContentResolver().notifyChange(uri, null);
			// getContext().getContentResolver().notifyChange(C.CONTENT_URI, null);
			return updated;
		} else if (uriType == C.MATCH_MAPS) {
			Log.d(TAG, "requested update of all maps? uri: " + uri);
			return 0;
		} else {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (uriMatcher.match(uri)) {
			case C.MATCH_MAPS:
			case C.MATCH_MAP_ID:
				break;

			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();// dbHelper.getReadableDatabase();
		Cursor c = db.query(C.DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		// Don't close the DB or the cursor will be closed as well.
		return c;
	}

	/*
	 * Helper class handling database creation and upgrade.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		Context context;

		DatabaseHelper(Context context) {
			super(context, C.DATABASE_NAME, null, C.DATABASE_VERSION);
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

}
