package com.inovex.zabbixmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class ZaxServerPreferences {

	private final SharedPreferences mPref;
	private final long serverId;

	public ZaxServerPreferences(Context context, long serverId) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		mPref = pref;
		this.serverId = serverId;

		loadPrefs();
	}

	private void loadPrefs() {
		Editor edit = mPref.edit();
		edit.putString("zabbix_url", getZabbixUrl());
		edit.commit();
	}

	public void savePrefs() {
		Editor edit = mPref.edit();
		edit.putString(serverId+"zabbix_url", mPref.getString("zabbix_url", null));
		edit.commit();
	}

	public boolean isConfigurated() {
		return !(getUsername().length() == 0)
				&& !(getZabbixUrl().length() == 0);
	}

	public String getUsername() {
		return mPref.getString("zabbix_username", "");
	}

	public String getPassword() {
		return mPref.getString("zabbix_password", "");
	}

	public boolean isTrustAllSSLCA() {
		return mPref.getBoolean("zabbix_trust_all_ssl_ca", false);
	}

	public boolean isHttpAuthEnabled() {
		return mPref.getBoolean("http_auth_enabled", false);
	}

	public String getHttpAuthUsername() {
		return mPref.getString("http_auth_username", "");
	}

	public String getHttpAuthPassword() {
		return mPref.getString("http_auth_password", "");
	}

	public String getZabbixUrl() {
		return mPref.getString(serverId+"zabbix_url", "");
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

	public String getZabbixAuthToken() {
		return mPref.getString("zabbix_auth_token", null);
	}

	public void setZabbixAuthToken(String token) {
		Editor edit = mPref.edit();
		edit.putString("zabbix_auth_token", token);
		edit.commit();
	}

	public void setZabbixVersion2(boolean version2) {
		Editor edit = mPref.edit();
		edit.putBoolean("zabbix_version2", version2);
		edit.commit();
	}

	public boolean isZabbixVersion2() {
		return mPref.getBoolean("zabbix_version2", true);
	}

}
