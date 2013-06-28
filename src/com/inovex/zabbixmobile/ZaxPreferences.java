package com.inovex.zabbixmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ZaxPreferences {

	public static String getUsername(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getString("zabbix_username", "");
	}

	public static String getPassword(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getString("zabbix_password", "");
	}

}
