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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	protected final static String TAG = ImageAdapter.class.getSimpleName();

	Context ctx;
	List<String> images;
	Map<String /* path */, BitmapDrawable> htImages;
	Random rand;

	public ImageAdapter(Context ctx) {
		this.ctx = ctx;
		this.images = new ArrayList<String>();
		this.htImages = new HashMap<String, BitmapDrawable>();
		this.rand = new Random(System.currentTimeMillis());
		Log.d(TAG, "initialized random generator");
	}

	public void addFromAssets(String rootPath) {
		synchronized (images) {
			try {
				String[] files = ctx.getAssets().list(rootPath);
				String random = "random_" + files.length;
				images.add(random);
				for (String name : files) {
					String fullpath = "assets:" + rootPath + "/" + name;
					images.add(fullpath);
					htImages.put(fullpath, new BitmapDrawable(ctx.getResources(), ctx.getAssets().open(rootPath + "/" + name)));
				}
				notifyDataSetChanged();
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}

	/*
	 * Adapter methods
	 */

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public Object getItem(int position) {
		String path = images.get(position);
		if (path.startsWith("random_")) {
			/* 7 = "random_".length() */
			int range = Integer.valueOf(path.substring(7));
			int r = position + 1 + rand.nextInt(range - 1);
			return images.get(r);
		}
		return images.get(position);
	}

	@Override
	public long getItemId(int position) {
		/* Not needed, just return the position */
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			// if it's not recycled, initialize some attributes
			imageView = new ImageView(ctx);
			int size = ctx.getResources().getDimensionPixelSize(R.dimen.selectDialog_grid_tile);
			imageView.setLayoutParams(new GridView.LayoutParams(size, size));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(2, 2, 2, 2);
		} else {
			imageView = (ImageView) convertView;
		}
		String path = images.get(position);
		if (path.startsWith("random_")) {
			/* Load "?" resource and merge it with the next resource */
			// imageView.setImageResource(R.drawable.ic_random);
			Drawable[] layers = new Drawable[2];
			// try {
			String auxPath = (String) getItem(position + 1);
			layers[0] = htImages.get(auxPath);
			layers[1] = ctx.getResources().getDrawable(R.drawable.ic_random);
			LayerDrawable layered = new LayerDrawable(layers);
			imageView.setImageDrawable(layered);
		} else {
			imageView.setImageDrawable(htImages.get(path));
		}
		return imageView;
	}
}
