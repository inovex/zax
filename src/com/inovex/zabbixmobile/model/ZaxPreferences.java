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
		return mPref.getBoolean("zabbix_push_old_icons", false);
	}

	public boolean isPushEnabled() {
		return mPref.getBoolean("zabbix_push_enabled", false);
	}

	public String getPushRingtone() {
		return mPref.getString("zabbix_push_ringtone", null);
	}

	public String getPushSubscribeKey() {
		return mPref.getString("zabbix_push_subscribe_key", "").trim();
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
