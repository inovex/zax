package com.inovex.zabbixmobile.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ZaxPreferences {
	
	private final SharedPreferences mPref;
	
	public ZaxPreferences(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		mPref = pref;
	}
	
	public boolean isConfigurated() {
		return !getUsername().isEmpty() && !getZabbixUrl().isEmpty();
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
}
