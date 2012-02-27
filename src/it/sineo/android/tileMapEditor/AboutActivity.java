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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity implements TabContentFactory, TabListener {

	@SuppressWarnings("unused")
	private final static String TAG = AboutActivity.class.getSimpleName();

	private final static String TAG_INFO = "info";
	private final static String TAG_CHANGELOG = "changelog";
	private final static String TAG_LICENSE = "license";

	private View tabInfo, tabChangelog, tabLicense;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		/* Action bar navigation */
		ActionBar bar = getSupportActionBar();
		bar.setHomeButtonEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);

		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab tab = bar.newTab();
		tab.setText(R.string.tab_info);
		tab.setTag(TAG_INFO);
		tab.setTabListener(this);
		bar.addTab(tab, true);

		tab = bar.newTab();
		tab.setText(R.string.tab_changelog);
		tab.setTag(TAG_CHANGELOG);
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText(R.string.tab_license);
		tab.setTag(TAG_LICENSE);
		tab.setTabListener(this);
		bar.addTab(tab);
	}

	public View createTabContent(String tag) {
		if (TAG_INFO.equals(tag)) {
			if (tabInfo == null) {
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
				// Linkify.addLinks((TextView) v.findViewById(R.id.about_info_homepage), Linkify.ALL);
				Linkify.addLinks((TextView) v.findViewById(R.id.about_info_extra), Linkify.ALL);

				tabInfo = v;
			}
			return tabInfo;
		} else if (TAG_CHANGELOG.equals(tag)) {
			if (tabChangelog == null) {
				tabChangelog = ChangelogFactory.inflate(this, R.xml.changelog);
			}
			return tabChangelog;
		} else if (TAG_LICENSE.equals(tag)) {
			if (tabLicense == null) {
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
				TextView license = (TextView) v.findViewById(R.id.about_license);
				license.setText(text);
				Linkify.addLinks(license, Linkify.WEB_URLS);
				tabLicense = v;
			}
			return tabLicense;
		}
		Log.e(getClass().getName(), "unknown tag: " + tag);
		return null;
	}

	@Override
	public void onTabSelected(Tab tab) {
		String tag = tab.getTag().toString();
		setContentView(createTabContent(tag));
	}

	@Override
	public void onTabUnselected(Tab tab) {
		// Do nothing.
	}

	@Override
	public void onTabReselected(Tab tab) {
		String tag = tab.getTag().toString();
		setContentView(createTabContent(tag));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				Intent intent = new Intent(this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}
}