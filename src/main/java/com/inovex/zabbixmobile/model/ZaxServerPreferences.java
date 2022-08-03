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

import com.inovex.zabbixmobile.data.ZabbixAPIVersion;

public class ZaxServerPreferences {

	private final SharedPreferences mPref;
	private final long serverId;

	public ZaxServerPreferences(Context context, long serverId, boolean notLoadPrefs) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		mPref = pref;
		this.serverId = serverId;

		if (!notLoadPrefs) loadPrefs();
	}

	private void loadPrefs() {
		Editor edit = mPref.edit();
		edit.putString("zabbix_url", getZabbixUrl());
		edit.putString("zabbix_username", getUsername());
		edit.putString("zabbix_password", getPassword());
		edit.putBoolean("http_auth_enabled", isHttpAuthEnabled());
		edit.putString("http_auth_username", getHttpAuthUsername());
		edit.putString("http_auth_password", getHttpAuthPassword());
		edit.commit();
	}

	public void savePrefs() {
		Editor edit = mPref.edit();
		edit.putString(serverId + "zabbix_url", mPref.getString("zabbix_url", ""));
		edit.putString(serverId + "zabbix_username", mPref.getString("zabbix_username", ""));
		edit.putString(serverId + "zabbix_password", mPref.getString("zabbix_password", ""));
		edit.putBoolean(serverId + "http_auth_enabled", mPref.getBoolean("http_auth_enabled", false));
		edit.putString(serverId + "http_auth_username", mPref.getString("http_auth_username", ""));
		edit.putString(serverId + "http_auth_password", mPref.getString("http_auth_password", ""));
		edit.commit();
	}

	public boolean isConfigurated() {
		return !(getUsername().length() == 0)
				&& !(getZabbixUrl().length() == 0);
	}

	public String getUsername() {
		Log.d("ZaxServerPreferences", "aaa=" + serverId);
		return mPref.getString(serverId + "zabbix_username", "");
	}

	public String getPassword() {
		return mPref.getString(serverId + "zabbix_password", "");
	}

	public boolean isHttpAuthEnabled() {
		return mPref.getBoolean(serverId + "http_auth_enabled", false);
	}

	public String getHttpAuthUsername() {
		return mPref.getString(serverId + "http_auth_username", "");
	}

	public String getHttpAuthPassword() {
		return mPref.getString(serverId + "http_auth_password", "");
	}

	public String getZabbixUrl() {
		return mPref.getString(serverId + "zabbix_url", "");
	}

	/**
	 * Checks whether the server settings have been altered by the user
	 *
	 * @return true: the server settings are still default
	 */
	public boolean isDefault() {
		String url = mPref.getString("zabbix_url", "");
		return (url.equals("http://zabbix.company.net/zabbix"))
				|| (url == null) || url.equals("");
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		mPref.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener listener) {
		mPref.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public void setZabbixAPIVersion(ZabbixAPIVersion version) {
		Editor edit = mPref.edit();
		edit.putInt("zabbix_api_version", version.getValue());
		edit.commit();
	}

	public ZabbixAPIVersion getZabbixAPIVersion() {
		return ZabbixAPIVersion.get(mPref.getInt("zabbix_api_version", ZabbixAPIVersion.API_1_3.getValue()));
	}


}
