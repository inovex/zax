package com.inovex.zabbixmobile.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Alarm to wake up the push service.
 *
 */
public class PushAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("PushServiceAlarm", "received alarm.");
		// start the push receiver, if it is enabled
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean push = prefs.getBoolean("zabbix_push_enabled", false);
		if (push) {
			PushService.startOrStopPushService(context, true);
			// we wait a few seconds to ensure that wifi was activated
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
