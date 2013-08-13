package com.inovex.zabbixmobile.push;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PushAlarm extends BroadcastReceiver {
	NotificationManager nm;

	@Override
	public void onReceive(Context context, Intent intent) {
		// start the push receiver, if it is enabled
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean push = prefs.getBoolean("zabbix_push_enabled", false);
		if (push) {
			Intent myIntent = new Intent(context, PushService.class);
			context.startService(myIntent);
		}
	}
}
