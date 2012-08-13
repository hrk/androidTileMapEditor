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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends it.sineo.android.common.AboutActivity {

	@SuppressWarnings("unused")
	private final static String TAG = AboutActivity.class.getSimpleName();

	private final static String TAG_INFO = "info";
	private final static String TAG_CHANGELOG = "changelog";
	private final static String TAG_LICENSE = "license";

	protected void addTabs() {
		addTab(TAG_INFO, R.string.tab_info);
		addTab(TAG_CHANGELOG, R.string.tab_changelog);
		addTab(TAG_LICENSE, R.string.tab_license);
	}

	protected TabFragment instantiateTabFragment() {
		return new TabFragment();
	}

	public static class TabFragment extends it.sineo.android.common.TabFragment {

		public View createTabContent(String tag, LayoutInflater inflater) {
			Context ctx = inflater.getContext();
			if (TAG_INFO.equals(tag)) {
				View v = inflater.inflate(R.layout.about_info, null, false);

				String version = "?";
				try {
					PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
					version = info.versionName;
				} catch (PackageManager.NameNotFoundException pmnnfex) {
					pmnnfex.printStackTrace();
				}
				((TextView) v.findViewById(R.id.about_info_version)).setText(getResources().getString(
						R.string.about_info_version, version));
				/* Donate button */
				ImageView ivDonate = (ImageView) v.findViewById(R.id.donate_button);
				ivDonate.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.paypal_donate_url)));
						startActivity(i);
					}
				});

				return v;
			} else if (TAG_CHANGELOG.equals(tag)) {
				return ChangelogFactory.inflate(ctx, R.xml.changelog);
			} else if (TAG_LICENSE.equals(tag)) {
				return createLicenseView(R.raw.license, inflater);
			}
			Log.e(getClass().getName(), "unknown tag: " + tag);
			return null;
		}
	}
}