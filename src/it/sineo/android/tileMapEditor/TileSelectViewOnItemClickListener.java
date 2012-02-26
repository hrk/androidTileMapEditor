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
import android.view.View;
import android.widget.AdapterView;

public class TileSelectViewOnItemClickListener implements AdapterView.OnItemClickListener {

	private AlertDialog dialog;
	private TiledMapView view;
	private int row;
	private int column;

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		String path = (String) ((ImageAdapter) parent.getAdapter()).getItem(position);
		view.setTile(row, column, path);
		dialog.dismiss();
	}

	/* get/set */
	public void setDialog(AlertDialog dialog) {
		this.dialog = dialog;
	}

	public void setView(TiledMapView view) {
		this.view = view;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setColumn(int column) {
		this.column = column;
	}
}
