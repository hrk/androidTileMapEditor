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

import android.content.ContentResolver;
import android.graphics.Color;
import android.net.Uri;

public class C extends it.sineo.android.common.C {
	public final static boolean DEVELOPER_MODE = false;

	public final static String EXTERNAL_DIR = "TiledMapEditor";
	public final static String EXTERNAL_DIR_THUMBNAILS = ".thumbs";

	public final static int REQ_CODE_SHARE = 1;
	public final static int REQ_CODE_NEW_MAP = 2;
	public final static int REQ_CODE_EDIT_MAP = 3;
	public final static int REQ_CODE_SELECT_EXTERNAL_TILE = 4;

	public final static String EXTRA_MAP_ROWS = C.class.getPackage().getName() + ".extra.map_rows";
	public final static String EXTRA_MAP_COLUMNS = C.class.getPackage().getName() + ".extra.map_columns";

	public final static String EXTRA_MAP_ID = C.class.getPackage().getName() + ".extra.map_id";
	/**
	 * 
	 */
	public final static String EXTRA_MAP_BUNDLE = C.class.getPackage().getName() + ".extra.map_bundle";
	public final static String EXTRA_MAP_JSON = C.class.getPackage().getName() + ".extra.map_json";
	public final static String EXTRA_MAP_THUMB = C.class.getPackage().getName() + ".extra.map_thumb";

	public final static int DIALOG_NEW_MAP = 1;
	public final static int DIALOG_CONFIRM_SAVE_MAP = 2;
	public final static int DIALOG_RENAME_MAP = 3;
	public final static int DIALOG_LOADING_MAPS = 4;

	public final static String PREFS_MAP_SHOW_GRID = "map_show_grid";
	public final static boolean DEFAULT_MAP_SHOW_GRID = true;
	public final static String PREFS_EXPORT_SHOW_GRID = "export_show_grid";
	public final static boolean DEFAULT_EXPORT_SHOW_GRID = true;

	public final static String PREFS_MAP_COLOR_EMPTY_TILE = "map_color_empty_tile";
	public final static int DEFAULT_MAP_COLOR_EMPTY_TILE = Color.DKGRAY;

	/*
	 * Database and content provider
	 */
	public final static String DATABASE_NAME = "TileMapEditor";
	public final static String DATABASE_TABLE = "t_map";
	public final static int DATABASE_VERSION = 1;

	public final static int MATCH_MAPS = 1;
	public final static int MATCH_MAP_ID = 2;

	public final static String AUTHORITY_MAP = TileMapContentProvider.class.getName();

	public final static String CONTENT_MAP_BASE = "tileMaps";
	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY_MAP + "/" + CONTENT_MAP_BASE);

	public final static String CONTENT_TYPE_MAP = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sineo.tileMap";

}
