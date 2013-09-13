package com.inovex.zabbixmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Singleton providing an interface to the application's shared preferences.
 * 
 */
public class ZaxPreferences {

	private final SharedPreferences mPref;

	private static ZaxPreferences instance;

	public static ZaxPreferences getInstance(Context context) {
		if (instance != null)
			return instance;
		return (new ZaxPreferences(context));
	}

	private ZaxPreferences(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		mPref = pref;
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
		return mPref.getString("zabbix_url", "");
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

	public int getWidgetRefreshInterval() {
		return Integer.parseInt(mPref.getString("widget_refresh_interval_mins",
				"15"));
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
}
