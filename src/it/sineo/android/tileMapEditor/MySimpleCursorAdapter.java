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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {

	private final static String TAG = MySimpleCursorAdapter.class.getSimpleName();

	public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.ctx = context;
	}

	private SimpleDateFormat sdfLastUpdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Context ctx;
	private LayoutInflater inflater;
	private Map<Long /* id */, Drawable /* thumbnail */> cache = new HashMap<Long, Drawable>();
	private Map<Long /* id */, Long /* lastUpdate */> cacheValidity = new HashMap<Long, Long>();

	private static class ViewHolder {
		TextView tvName, tvDate;
		ImageView ivThumb;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			if (inflater == null) {
				inflater = LayoutInflater.from(ctx);
			}
			convertView = inflater.inflate(R.layout.home_grid_item, null);
		}
		ViewHolder tag = (ViewHolder) convertView.getTag();
		if (tag == null) {
			tag = new ViewHolder();
			tag.tvName = (TextView) convertView.findViewById(R.id.home_grid_item_name);
			tag.tvDate = (TextView) convertView.findViewById(R.id.home_grid_item_date);
			tag.ivThumb = (ImageView) convertView.findViewById(R.id.home_grid_item_thumb);
			convertView.setTag(tag);
		}

		getCursor().moveToPosition(position);

		long id = getCursor().getLong(0);
		String name = getCursor().getString(1);
		long lastUpdate = getCursor().getLong(3);

		tag.tvName.setText(name);
		tag.tvDate.setText(sdfLastUpdate.format(lastUpdate));

		Drawable thumb;
		if (!cache.containsKey(id) || cacheValidity.get(id) < lastUpdate) {
			/* Not loaded yet, or changed after being cached: load it again */
			try {
				File thumbFile = new File(Util.getExternalThumbnailsDirectory(), "tn_" + id + ".png");
				if (!thumbFile.exists()) {
					thumb = ctx.getResources().getDrawable(R.drawable.not_found);
				} else {
					Bitmap bmp = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
					if (bmp != null) {
						thumb = new BitmapDrawable(ctx.getResources(), bmp);
					} else {
						thumb = ctx.getResources().getDrawable(R.drawable.not_found);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(TAG, "exception loading thumb: " + ex.getMessage());
				thumb = ctx.getResources().getDrawable(R.drawable.not_found);
			}
			cache.put(id, thumb);
			cacheValidity.put(id, lastUpdate);
		} else {
			/* In the cache and valid */
			thumb = cache.get(id);
		}
		tag.ivThumb.setImageDrawable(thumb);

		return convertView;
	}
}
