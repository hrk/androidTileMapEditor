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

import it.sineo.android.changelog.ChangelogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.TabActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

public class AboutActivity extends  TabActivity implements TabContentFactory {

	private final static String TAG_INFO = "info";
	private final static String TAG_CHANGELOG = "changelog";
	// private final static String TAG_FAQ = "faq";
	private final static String TAG_LICENSE = "license";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		TabHost.TabSpec tab = getTabHost().newTabSpec(TAG_INFO);
		tab.setIndicator(getString(R.string.tab_info));
		tab.setContent(this); // Fixed.
		getTabHost().addTab(tab);

		tab = getTabHost().newTabSpec(TAG_CHANGELOG);
		tab.setIndicator(getString(R.string.tab_changelog));
		tab.setContent(this); // Dynamic.
		getTabHost().addTab(tab);

		tab = getTabHost().newTabSpec(TAG_LICENSE);
		tab.setIndicator(getString(R.string.tab_license));
		tab.setContent(this); // Dynamic.
		getTabHost().addTab(tab);

		getTabHost().setCurrentTab(0);
	}

	public View createTabContent(String tag) {
		if (TAG_INFO.equals(tag)) {
			LayoutInflater li = LayoutInflater.from(this);
			View v = li.inflate(R.layout.about_info, null);

			String version = "?";
			try {
				PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
				version = info.versionName;
			} catch (PackageManager.NameNotFoundException pmnnfex) {
				pmnnfex.printStackTrace();
			}
			((TextView) v.findViewById(R.id.about_info_version)).setText(getResources().getString(
					R.string.about_info_version, version));
			Linkify.addLinks((TextView) v.findViewById(R.id.about_info_author), Linkify.ALL);
			Linkify.addLinks((TextView) v.findViewById(R.id.about_info_homepage), Linkify.ALL);
			Linkify.addLinks((TextView) v.findViewById(R.id.about_info_extra), Linkify.ALL);
			final ImageView icon = (ImageView) v.findViewById(R.id.about_info_icon);
			icon.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					String tag = (String) icon.getTag();
					if (tag == null || "new".equals(tag)) {
						icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_hrk));
						icon.setTag("old");
					} else {
						icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
						icon.setTag("new");
					}
					return false;
				}
			});

			return v;
		} else if (TAG_CHANGELOG.equals(tag)) {
			return ChangelogFactory.inflate(this, R.xml.changelog);
			// } else if (TAG_FAQ.equals(tag)) {
			// LayoutInflater li = LayoutInflater.from(this);
			// View v = li.inflate(R.layout.about_faq, null);
			// Linkify.addLinks((TextView) v.findViewById(R.id.about_faq_governors),
			// Linkify.ALL);
			// Linkify.addLinks((TextView) v.findViewById(R.id.about_faq_profiles),
			// Linkify.ALL);
			//
			// return v;
		} else if (TAG_LICENSE.equals(tag)) {
			String text = "";
			try {
				InputStream is = getResources().openRawResource(R.raw.license);
				BufferedReader br = new BufferedReader(new InputStreamReader(is), 4096);
				String line = null;
				while ((line = br.readLine()) != null) {
					text += line + "\r\n";
				}
				br.close();
				is.close();
			} catch (IOException ioex) {
				text = ioex.getLocalizedMessage();
			}
			LayoutInflater li = LayoutInflater.from(this);
			View v = li.inflate(R.layout.about_license, null);
			((TextView) v.findViewById(R.id.about_license)).setText(text);
			return v;
		}
		Log.e(getClass().getName(), "unknown tag: " + tag);
		return null;
	}
}
