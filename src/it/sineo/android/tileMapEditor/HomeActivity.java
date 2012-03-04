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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;
import android.widget.Toast;

import com.michaelnovakjr.numberpicker.NumberPicker;

public class HomeActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

	private String TAG = HomeActivity.class.getSimpleName();

	private GridView gvPreview;
	private View emptyGrid;
	private MySimpleCursorAdapter adapter;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (C.DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeathOnNetwork()
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		gvPreview = (GridView) findViewById(R.id.home_grid);
		/* Empty grid view */
		emptyGrid = getLayoutInflater().inflate(R.layout.home_grid_empty, null);
		emptyGrid.findViewById(R.id.home_grid_empty).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(C.DIALOG_NEW_MAP);
			}
		});
		addContentView(emptyGrid, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		gvPreview.setEmptyView(emptyGrid);

		/* Real grid view, with background loading */
		getSupportLoaderManager().initLoader(0, null, this);

		String[] cols = new String[] {
				"_id", "_name", "_json_data", "_last_update",
		};

		adapter = new MySimpleCursorAdapter(getApplicationContext(), R.layout.home_grid_item, null, cols, null, 0);

		gvPreview.setAdapter(adapter);
		gvPreview.setOnItemClickListener(this);

		/* Context menu */
		registerForContextMenu(gvPreview);

		// showDialog(C.DIALOG_LOADING_MAPS);
		progressDialog = ProgressDialog.show(HomeActivity.this, null, getString(R.string.home_dlg_loading_maps), true);
	}

	/*
	 * LoaderCallback
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = {
				TileMap.Columns.KEY_ROWID,
				TileMap.Columns.KEY_NAME,
				TileMap.Columns.KEY_JSON_DATA,
				TileMap.Columns.KEY_LAST_UPDATE,
		};
		CursorLoader cursorLoader = new CursorLoader(this, C.CONTENT_URI, projection, null, null, "_last_update DESC");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent tiledMapActivity = new Intent(HomeActivity.this, TiledMapActivity.class);
		Cursor cur = (Cursor) parent.getAdapter().getItem(position);
		long mapid = cur.getLong(0);
		String json = cur.getString(2);
		tiledMapActivity.putExtra(C.EXTRA_MAP_JSON, json);
		tiledMapActivity.putExtra(C.EXTRA_MAP_ID, mapid);
		startActivityForResult(tiledMapActivity, C.REQ_CODE_EDIT_MAP);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_ctx, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.home_ctx_menu_delete: {
				AdapterContextMenuInfo cmi = (AdapterContextMenuInfo) item.getMenuInfo();
				Uri mapUri = Uri.withAppendedPath(C.CONTENT_URI, Long.toString(cmi.id));
				getContentResolver().delete(mapUri, null, null);
				return true;
			}
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		LayoutInflater inflater = getLayoutInflater();
		switch (id) {
			case C.DIALOG_NEW_MAP: {
				View dlgView = (View) inflater.inflate(R.layout.home_dlg_new, null);
				final NumberPicker npRows = (NumberPicker) dlgView.findViewById(R.id.home_dlg_rows);
				final NumberPicker npColumns = (NumberPicker) dlgView.findViewById(R.id.home_dlg_columns);
				npRows.setRange(1, 20);
				npRows.setCurrent(2);
				npColumns.setRange(1, 20);
				npColumns.setCurrent(2);

				AlertDialog.Builder bldr = new AlertDialog.Builder(HomeActivity.this);
				bldr.setView(dlgView);
				DialogInterface.OnClickListener newMaplistener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE: {
								/* Memory check */
								int tileSize = getResources().getDimensionPixelSize(R.dimen.tiledMap_tile);
								int columns = npColumns.getCurrent();
								int rows = npRows.getCurrent();
								try {
									Bitmap.createBitmap(columns * tileSize, rows * tileSize, Bitmap.Config.ARGB_8888).recycle();
									/* Ok, proceed */
									Intent tiledMapActivity = new Intent(HomeActivity.this, TiledMapActivity.class);
									tiledMapActivity.putExtra(C.EXTRA_MAP_ROWS, rows);
									tiledMapActivity.putExtra(C.EXTRA_MAP_COLUMNS, columns);
									startActivityForResult(tiledMapActivity, C.REQ_CODE_NEW_MAP);
								} catch (OutOfMemoryError oomer) {
									/* Not enough memory to draw this map */
									Toast.makeText(HomeActivity.this, getString(R.string.home_dlg_new_oom, rows, columns),
											Toast.LENGTH_LONG).show();
								}
								break;
							}
							case DialogInterface.BUTTON_NEGATIVE: {
								Toast.makeText(HomeActivity.this, R.string.home_dlg_new_canceled, Toast.LENGTH_SHORT).show();
								break;
							}
						}
					}
				};
				bldr.setPositiveButton(android.R.string.ok, newMaplistener);
				bldr.setNegativeButton(android.R.string.cancel, newMaplistener);
				dialog = bldr.create();
				break;
			}
			default: {
				dialog = super.onCreateDialog(id);
				break;
			}
		}
		return dialog;
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.global_menu_info: {
				Intent aboutActivity = new Intent(HomeActivity.this, AboutActivity.class);
				startActivity(aboutActivity);
				return true;
			}
			case R.id.global_menu_settings: {
				Intent settingsActivity = new Intent(HomeActivity.this, SettingsActivity.class);
				startActivity(settingsActivity);
				return true;
			}
			case R.id.home_menu_new: {
				/* Ask user for rows, columns and map name */
				showDialog(C.DIALOG_NEW_MAP);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == C.REQ_CODE_NEW_MAP) {
			if (resultCode == RESULT_OK && data != null) {
				String mapJSON = data.getStringExtra(C.EXTRA_MAP_JSON);
				TileMap map = new TileMap(mapJSON);

				ContentValues values = new ContentValues();
				values.put(TileMap.Columns.KEY_NAME, map.name);
				values.put(TileMap.Columns.KEY_JSON_DATA, mapJSON);
				values.put(TileMap.Columns.KEY_THUMB, data.getByteArrayExtra(C.EXTRA_MAP_THUMB));
				getContentResolver().insert(C.CONTENT_URI, values);
			}
			Log.d(TAG, "received code " + resultCode + " w/ data: " + data);
		} else if (requestCode == C.REQ_CODE_EDIT_MAP) {
			if (resultCode == RESULT_OK && data != null) {
				String mapJSON = data.getStringExtra(C.EXTRA_MAP_JSON);
				long id = data.getLongExtra(C.EXTRA_MAP_ID, -1);
				TileMap map = new TileMap(mapJSON);
				ContentValues values = new ContentValues();
				values.put(TileMap.Columns.KEY_NAME, map.name);
				values.put(TileMap.Columns.KEY_JSON_DATA, mapJSON);
				values.put(TileMap.Columns.KEY_THUMB, data.getByteArrayExtra(C.EXTRA_MAP_THUMB));
				Uri mapUri = Uri.withAppendedPath(C.CONTENT_URI, Long.toString(id));
				getContentResolver().update(mapUri, values, null, null);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
