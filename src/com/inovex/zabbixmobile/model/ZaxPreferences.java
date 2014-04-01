package com.inovex.zabbixmobile.model;

import java.util.ArrayList;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.inovex.zabbixmobile.util.ObjectSerializer;

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

	public void setServers(TreeSet<ZabbixServer> servers) {
		Editor edit = mPref.edit();
		edit.putString("servers", ObjectSerializer.objectToString(servers));
		edit.commit();
	}

	public TreeSet<ZabbixServer> getServers() {
		return ObjectSerializer.stringToObject(mPref.getString("servers", ""));
	}

	public void addServer(ZabbixServer server) {
		TreeSet<ZabbixServer> servers = getServers();
		servers.add(server);
		setServers(servers);
	}

	public void removeServer(ZabbixServer server) {
		TreeSet<ZabbixServer> servers = getServers();
		servers.remove(server);
		setServers(servers);
	}
	
	public int getServerSelection() {
		return mPref.getInt("server_selection", 0);
	}
	
	public void setServerSelection(int selection) {
		Editor edit = mPref.edit();
		edit.putInt("server_selection", selection);
		edit.commit();
	}

}
