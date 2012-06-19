package com.inovex.zabbixmobile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.inovex.zabbixmobile.push.PushAlarm;
import com.inovex.zabbixmobile.push.PushService;

/**
 * receiver to start the push service directly after the system boot
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
	private AlarmManager am;

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			// start the push receiver, if it is enabled
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			boolean push = prefs.getBoolean("zabbix_push_enabled", false);
			if (push) {
				am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				setRepeatingAlarm(context);
			}
		}
	}

	public void setRepeatingAlarm(Context context) {
		Intent messageservice = new Intent(context, PushService.class);
		context.startService(messageservice);

		Intent intent = new Intent(context, PushAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				(1 * 60 * 1000), pendingIntent); //wake up every 5 minutes to ensure service stays alive
	}
}
