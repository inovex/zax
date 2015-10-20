/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Singleton providing an interface to the application's shared preferences.
 *
 */
public class ZaxPreferences {

	private SharedPreferences mPref;

	public static ZaxPreferences getInstance(Context context) {
		return (new ZaxPreferences(context));
	}

	private ZaxPreferences(Context context) {
		refresh(context);
	}

	public int getWidgetRefreshInterval() {
		try {
			return Integer.parseInt(mPref.getString(
					"widget_refresh_interval_mins", "15"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		mPref.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		mPref.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public boolean isDarkTheme() {
		return mPref.getBoolean("dark_theme", false);
	}

	public long getServerSelection() {
		return mPref.getLong("server_selection", 0);
	}

	public void setServerSelection(long selection) {
		Editor edit = mPref.edit();
		edit.putLong("server_selection", selection);
		edit.commit();
	}

	public void refresh(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		mPref = pref;
	}

	public boolean hasOldServerPreferences() {
		if (mPref.getString("zabbix_url", null) != null) {
			return true;
		}
		return false;
	}

	public void migrateServerPreferences(Context context, long id) {
		ZaxServerPreferences p = new ZaxServerPreferences(context, id, true);
		p.savePrefs();

		refresh(context);
		mPref.edit().remove("zabbix_url").commit();
	}

	public boolean isOldNotificationIcons() {
		return mPref.getBoolean("push_old_icons", false);
	}

	public boolean isPushEnabled() {
		return mPref.getBoolean("pubnub_push_enabled", false);
	}

	public String getPushRingtone() {
		return mPref.getString("push_ringtone", null);
	}

	public String getPushOkRingtone(){
		return mPref.getString("push_ok_ringtone", null);
	}

	public String getPushSubscribeKey() {
		return mPref.getString("pubnub_push_subscribe_key", "").trim();
	}

	public void setWidgetServer(int mAppWidgetId, long id) {
		Editor edit = mPref.edit();
		edit.putLong("widget_server_"+mAppWidgetId, id);
		edit.commit();
		Log.d("ZaxPreferences", "widget server="+mAppWidgetId+"="+id);
	}

	public long getWidgetServer(int mAppWidgetId) {
		Log.d("ZaxPreferences", "get widget server from "+mAppWidgetId+"="+mPref.getLong("widget_server_"+mAppWidgetId, -1));

		return mPref.getLong("widget_server_"+mAppWidgetId, -1);
	}

}
