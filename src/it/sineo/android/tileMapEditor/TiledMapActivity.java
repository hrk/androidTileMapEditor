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
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

public class TiledMapActivity extends SherlockFragmentActivity implements DialogInterface.OnClickListener {
	private static final String TAG = "TiledMapActivity";

	TiledMapView view = null;
	ImageAdapter adapter = null;
	long mapId = -1;

	/*
	 * Dialogs
	 */
	private Dialog renameDialog, confirmMapSaveDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tilemap);

		/* Action bar navigation */
		ActionBar bar = getSupportActionBar();
		bar.setHomeButtonEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);

		view = (TiledMapView) findViewById(R.id.tiledmap);
		view.setOnShortPressListener(shortPressListener);
		view.setOnLongPressListener(longPressListener);

		/*
		 * If this is a new map, resize to the requested values, otherwise decode
		 * from the opened map data
		 */
		Intent i = getIntent();
		if (i.hasExtra(C.EXTRA_MAP_ID)) {
			mapId = i.getLongExtra(C.EXTRA_MAP_ID, -1);
			view.restoreFromJSON(i.getStringExtra(C.EXTRA_MAP_JSON));
			getSupportActionBar().setTitle(view.getMapName());
		} else {
			int rows = i.getIntExtra(C.EXTRA_MAP_ROWS, -1);
			int columns = i.getIntExtra(C.EXTRA_MAP_COLUMNS, -1);
			if (rows != -1 && columns != -1) {
				view.initMap(rows, columns);
			} else {
				Log.e(TAG, MessageFormat.format("missing dimensions (rows={0}, columns={1}", rows, columns));
			}
			showDialog(C.DIALOG_RENAME_MAP);
		}

		adapter = new ImageAdapter(TiledMapActivity.this);
		try {
			String[] tileSets = getAssets().list("gfx");
			for (String tileSet : tileSets) {
				adapter.addFromAssets("gfx/" + tileSet);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		// adapter.addFromAssets("gfx/roads");
		// adapter.addFromAssets("gfx/roads_2");
		// adapter.addFromAssets("gfx/zerloon");

		registerForContextMenu(view);
	}

	@Override
	protected void onDestroy() {
		if (adapter != null && adapter.htImages != null) {
			for (BitmapDrawable bd : adapter.htImages.values()) {
				bd.getBitmap().recycle();
			}
			adapter.htImages.clear();
			adapter.images.clear();
		}
		super.onDestroy();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Bundle tileMapBundle = savedInstanceState.getBundle("tileMap");
		Log.d(TAG, "savedInstanceState contains: " + tileMapBundle);
		view.restoreFromBundle(tileMapBundle);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Bundle tileMapBundle = view.toBundle();
		Log.d(TAG, "view returned: " + tileMapBundle);
		outState.putBundle("tileMap", tileMapBundle);
		super.onSaveInstanceState(outState);
	}

	/* Back button handling */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			/*
			 * Take care of calling this method on earlier versions of the platform
			 * where it doesn't exist.
			 */
			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		/*
		 * Do not call super.onBackPressed(); and handle Activity.finish() inside
		 * the dialog listener
		 */
		showDialog(C.DIALOG_CONFIRM_SAVE_MAP);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == renameDialog) {
			/* Rename map */
			final EditText input = (EditText) renameDialog.findViewById(R.id.tiledMap_dlg_rename_name);
			String mapName = input.getText().toString().trim();
			/* Fix the input to avoid empty strings */
			if (mapName.length() == 0) {
				mapName = getString(R.string.tiledMap_dlg_renameMap_noName);
			}
			if (which == DialogInterface.BUTTON_POSITIVE) {
				view.renameMap(mapName);
				getSupportActionBar().setTitle(mapName);
			} else if (which == DialogInterface.BUTTON_NEGATIVE
					&& (view.getMapName() == null || view.getMapName().trim().length() == 0)) {
				/* Hit "Cancel", but the map lacks a name */
				mapName = getString(R.string.tiledMap_dlg_renameMap_noName);
				view.renameMap(mapName);
				getSupportActionBar().setTitle(mapName);
			}
			/* End of rename map */
		} else if (dialog == confirmMapSaveDialog) {
			/* Confirm map save */
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					/* Save changes */
					Intent data = new Intent();
					data.putExtra(C.EXTRA_MAP_ID, mapId);
					data.putExtra(C.EXTRA_MAP_JSON, view.toJSON());
					data.putExtra(C.EXTRA_MAP_THUMB, view.exportThumb(CompressFormat.PNG, 9));
					TiledMapActivity.this.setResult(RESULT_OK, data);
					TiledMapActivity.this.finish();
					//
					break;
				}
				case DialogInterface.BUTTON_NEGATIVE: {
					/* Discard changes */
					TiledMapActivity.this.setResult(RESULT_CANCELED);
					TiledMapActivity.this.finish();
					break;
				}
			}
			/* End of confirm save changes */
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case C.DIALOG_CONFIRM_SAVE_MAP: {
				AlertDialog.Builder bldr = new AlertDialog.Builder(TiledMapActivity.this);
				bldr.setIcon(android.R.drawable.ic_dialog_alert);
				bldr.setTitle(R.string.tiledMap_dlg_confirmSave_title);
				bldr.setPositiveButton(R.string.tiledMap_dlg_confirmSave_positive, this);
				bldr.setNegativeButton(R.string.tiledMap_dlg_confirmSave_negative, this);
				bldr.setMessage(R.string.tiledMap_dlg_confirmSave_message);
				confirmMapSaveDialog = bldr.create();
				return confirmMapSaveDialog;
			}
			case C.DIALOG_RENAME_MAP: {
				View content = getLayoutInflater().inflate(R.layout.tilemap_dlg_rename, null);
				final EditText input = (EditText) content.findViewById(R.id.tiledMap_dlg_rename_name);

				String mapName = view.getMapName();
				if (mapName == null || mapName.trim().length() == 0) {
					mapName = getString(R.string.tiledMap_dlg_renameMap_noName);
				}
				input.setText(mapName);
				input.setSelection(0, mapName.length());
				AlertDialog.Builder bldr = new AlertDialog.Builder(TiledMapActivity.this);
				// bldr.setTitle("Rename map");
				// bldr.setMessage("Enter the name for the map");
				bldr.setView(content);
				bldr.setCancelable(false);
				bldr.setPositiveButton(android.R.string.ok, this);
				bldr.setNegativeButton(android.R.string.cancel, this);
				renameDialog = bldr.create();
				return renameDialog;
			}
			default: {
				return super.onCreateDialog(id);
			}
		}
	}

	private TiledMapView.OnShortPressListener shortPressListener = new TiledMapView.OnShortPressListener() {
		@Override
		public boolean onShortPress(final int row, final int column, final boolean isEmpty) {
			if (isEmpty) {
				LayoutInflater li = getLayoutInflater();

				TileSelectViewOnItemClickListener listener = new TileSelectViewOnItemClickListener();

				GridView grid = (GridView) li.inflate(R.layout.select_grid, null);
				grid.setAdapter(adapter);
				grid.setOnItemClickListener(listener);

				AlertDialog.Builder builder = new AlertDialog.Builder(TiledMapActivity.this);
				builder.setView(grid);
				AlertDialog dialog = builder.create();
				listener.setDialog(dialog);
				listener.setView(view);
				listener.setRow(row);
				listener.setColumn(column);
				dialog.show();
			} else {
				// Toast.makeText(getApplicationContext(),
				// "tile is not empty, rotate it", Toast.LENGTH_SHORT).show();
				view.rotateTile(row, column);
			}
			return true;
		}
	};

	private int longPressedRow = -1, longPressedColumn = -1;

	private TiledMapView.OnLongPressListener longPressListener = new TiledMapView.OnLongPressListener() {
		@Override
		public boolean onLongPress(int row, int column, boolean isEmpty) {
			if (isEmpty) {
				return false;
			} else {
				// view.setTile(row, column, null);
				longPressedRow = row;
				longPressedColumn = column;
				// openContextMenu(view);
				return true;
			}
		}
	};

	public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tiledmap_ctx, menu);
	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.tiledMap_ctx_menu_delete) {
			view.setTile(longPressedRow, longPressedColumn, null);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.tiledmap, menu);
		/* Enable "Save to SD" only if the SD is available */
		menu.findItem(R.id.tiledMap_menu_save).setEnabled(
				Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
		return true;
	};

	protected class ExportMapAsyncTask extends AsyncTask<String, Void, File> {
		protected final static int MODE_SAVE = 1;
		protected final static int MODE_SHARE = 2;

		private int mode = MODE_SAVE;
		private String filename;

		private ProgressDialog dlg;

		public ExportMapAsyncTask(String filename, int mode) {
			this.mode = mode;
			this.filename = filename;
		}

		@Override
		protected void onPreExecute() {
			dlg = ProgressDialog.show(TiledMapActivity.this, "", getText(R.string.tiledMap_export_dlg_message), true);
			dlg.show();
			super.onPreExecute();
		}

		@Override
		protected File doInBackground(String... params) {
			try {
				File destDirectory = Util.getExternalStorageDirectory();
				File destFile = new File(destDirectory, filename);

				byte[] pngData = view.export(Bitmap.CompressFormat.PNG, 90);
				FileOutputStream fos = new FileOutputStream(destFile);
				fos.write(pngData);
				fos.flush();
				fos.close();
				Log.d(TAG, "image size in bytes: " + destFile.length());
				return destFile;
			} catch (IOException ioex) {
				ioex.printStackTrace();
				Toast.makeText(TiledMapActivity.this,
						String.format(getString(R.string.tiledMap_export_ioerror), ioex.getMessage()), Toast.LENGTH_LONG).show();
				return null;
			} catch (OutOfMemoryError oome) {
				oome.printStackTrace();
				Toast.makeText(TiledMapActivity.this, R.string.tiledMap_export_oom, Toast.LENGTH_LONG).show();
				return null;
			}
		}

		@Override
		protected void onPostExecute(File result) {
			super.onPostExecute(result);
			if (dlg != null) {
				dlg.dismiss();
				dlg = null;
			}
			if (result != null) {
				if (mode == MODE_SHARE) {
					Uri uri = Uri.fromFile(result);
					Log.d(TAG, "using uRI: " + uri.toString());

					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("image/png");
					shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
					startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.tiledMap_chooser_share)),
							C.REQ_CODE_SHARE);
				} else {
					Log.d(TAG, result.getAbsolutePath());
					String test = String.format(getString(R.string.tiledMap_export_save_success), result.getAbsolutePath());
					Log.d(TAG, test);
					Toast.makeText(TiledMapActivity.this,
							String.format(getString(R.string.tiledMap_export_save_success), result.getAbsolutePath()),
							Toast.LENGTH_SHORT).show();
				}
			} // end-if: result is null, error has already been shown.
		}

	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				/*
				 * Do not navigate back home, but show dialog which handles finish() w/
				 * result.
				 */
				showDialog(C.DIALOG_CONFIRM_SAVE_MAP);
				return true;
			}
			case R.id.tiledMap_menu_save: {
				// TODO: factorize this.
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HHmm");
				// TODO: generate a new and unique name.
				ExportMapAsyncTask task = new ExportMapAsyncTask("TiledMap_" + sdf.format(System.currentTimeMillis()) + ".png",
						ExportMapAsyncTask.MODE_SAVE);
				task.execute();
				return true;
			}
			case R.id.tiledMap_menu_share: {
				ExportMapAsyncTask task = new ExportMapAsyncTask("TiledMap_shared.png", ExportMapAsyncTask.MODE_SHARE);
				task.execute();
				return true;
			}
			case R.id.tiledMap_menu_rename: {
				showDialog(C.DIALOG_RENAME_MAP);
				return true;
			}
			case R.id.global_menu_settings: {
				Intent settingsActivity = new Intent(TiledMapActivity.this, SettingsActivity.class);
				startActivity(settingsActivity);
				return true;
			}
			case R.id.global_menu_info: {
				Intent aboutActivity = new Intent(TiledMapActivity.this, AboutActivity.class);
				startActivity(aboutActivity);
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case C.REQ_CODE_SHARE: {
				if (Activity.RESULT_CANCELED == resultCode) {
					File destDirectory = Util.getExternalStorageDirectory();
					File destFile = new File(destDirectory, "TiledMap_shared.png");
					destFile.delete();
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}