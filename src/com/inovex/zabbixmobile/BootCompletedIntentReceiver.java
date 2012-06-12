package com.inovex.zabbixmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inovex.zabbixmobile.api.PushReceiverService;

/**
 * receiver to start the push service directly after the system boot
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			// start the push receiver, if it is enabled
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean push = prefs.getBoolean("zabbix_push_enabled", false);
			if (push) {
				Intent pushIntent = new Intent(context, PushReceiverService.class);
				context.startService(pushIntent);
			}
		}
	}
}
