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

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.BaseColumns;

public class TileMap {
	public static class Columns implements BaseColumns {
		/*
		 * Public constants
		 */
		public static final String KEY_ROWID = "_id";
		public static final String KEY_NAME = "_name";
		public static final String KEY_JSON_DATA = "_json_data";
		public static final String KEY_LAST_UPDATE = "_last_update";
		public static final String KEY_THUMB = "_thumb";
	}

	String name;
	/* Map info, mapped to JSON data */
	int rows;
	int columns;

	float scale = 1f;
	float xOff = 0;
	float yOff = 0;

	String[][] tilePaths;
	byte[][] tileAngles;
	/* Transient data */
	transient Bitmap[][] tileBitmaps;
	transient Matrix[][] tileMatrices;

	/**
	 * Returns a JSON representation of this map. No transient data is stored.
	 * 
	 * @return
	 */
	public String toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("name", name);

			o.put("rows", rows);
			o.put("columns", columns);
			o.put("scale", scale);
			o.put("xOff", xOff);
			o.put("yOff", yOff);
			for (int idxRow = 0; idxRow < rows; idxRow++) {
				for (int idxCol = 0; idxCol < columns; idxCol++) {
					if (tilePaths[idxRow][idxCol] != null) {
						o.put("paths_" + idxRow + "_" + idxCol, tilePaths[idxRow][idxCol]);
						o.put("angles_" + idxRow + "_" + idxCol, tileAngles[idxRow][idxCol]);
					}
				}
			}
		} catch (JSONException jsonex) {
			jsonex.printStackTrace();
		}
		String json = o.toString();
		return json;
	}

	/**
	 * Returns a Bundle representation of this map. No transient data is stored.
	 * 
	 * @return
	 */
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("name", name);

		b.putInt("rows", rows);
		b.putInt("columns", columns);
		b.putFloat("scale", scale);
		b.putFloat("xOff", xOff);
		b.putFloat("yOff", yOff);
		for (int idxRow = 0; idxRow < rows; idxRow++) {
			for (int idxCol = 0; idxCol < columns; idxCol++) {
				if (tilePaths[idxRow][idxCol] != null) {
					b.putString("paths_" + idxRow + "_" + idxCol, tilePaths[idxRow][idxCol]);
					b.putByte("angles_" + idxRow + "_" + idxCol, tileAngles[idxRow][idxCol]);
				}
			}
		}
		return b;
	}

	public TileMap(Bundle b) {
		name = b.getString("name");

		rows = b.getInt("rows");
		columns = b.getInt("columns");
		scale = b.getFloat("scale");
		xOff = b.getFloat("xOff");
		yOff = b.getFloat("yoff");

		tilePaths = new String[rows][columns];
		tileBitmaps = new Bitmap[rows][columns];
		tileAngles = new byte[rows][columns];
		tileMatrices = new Matrix[rows][columns];

		for (int idxRow = 0; idxRow < rows; idxRow++) {
			for (int idxCol = 0; idxCol < columns; idxCol++) {
				if (b.containsKey("paths_" + idxRow + "_" + idxCol)) {
					tilePaths[idxRow][idxCol] = b.getString("paths_" + idxRow + "_" + idxCol);
					tileAngles[idxRow][idxCol] = b.getByte("angles_" + idxRow + "_" + idxCol);
				}
			}
		}
	}

	public TileMap(String json) {
		try {
			JSONObject o = new JSONObject(json);

			name = o.getString("name");

			rows = o.getInt("rows");
			columns = o.getInt("columns");
			scale = (float) o.getDouble("scale");
			xOff = (float) o.getDouble("xOff");
			yOff = (float) o.getDouble("yOff");

			tilePaths = new String[rows][columns];
			tileBitmaps = new Bitmap[rows][columns];
			tileAngles = new byte[rows][columns];
			tileMatrices = new Matrix[rows][columns];

			for (int idxRow = 0; idxRow < rows; idxRow++) {
				for (int idxCol = 0; idxCol < columns; idxCol++) {
					if (o.has("paths_" + idxRow + "_" + idxCol)) {
						tilePaths[idxRow][idxCol] = o.getString("paths_" + idxRow + "_" + idxCol);
						tileAngles[idxRow][idxCol] = (byte) o.getInt("angles_" + idxRow + "_" + idxCol);
					}
				}
			}
		} catch (JSONException jsonex) {
			/* This cannot happen, unless the object was tampered with. */
			jsonex.printStackTrace();
		}
	}

	public TileMap(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;

		tilePaths = new String[rows][columns];
		tileBitmaps = new Bitmap[rows][columns];
		tileAngles = new byte[rows][columns];
		tileMatrices = new Matrix[rows][columns];
	}
}