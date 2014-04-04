package com.inovex.zabbixmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

/**
 * Singleton providing an interface to the application's shared preferences.
 *
 */
public class ZaxPreferences {

	private SharedPreferences mPref;

	private static ZaxPreferences instance;

	public static ZaxPreferences getInstance(Context context) {
		if (instance != null)
			return instance;
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

}
