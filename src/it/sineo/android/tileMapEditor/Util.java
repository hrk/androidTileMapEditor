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
import java.io.IOException;

import android.os.Environment;

public class Util {
	protected static File getExternalStorageDirectory() {
		File sdcard = Environment.getExternalStorageDirectory();
		File _externalSD = new File(sdcard, "_externalSD");
		if (_externalSD.exists()) {
			sdcard = _externalSD;
		}
		File destDirectory = new File(sdcard, C.EXTERNAL_DIR);
		if (!destDirectory.exists()) {
			destDirectory.mkdirs();
		}
		return destDirectory;
	}

	protected static File getExternalThumbnailsDirectory() {
		File homeDir = getExternalStorageDirectory();
		File thumbsDir = new File(homeDir, C.EXTERNAL_DIR_THUMBNAILS);
		thumbsDir.mkdirs();
		File nomediaFile = new File(thumbsDir, ".nomedia");
		try {
			nomediaFile.createNewFile();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		return thumbsDir;
	}

}
