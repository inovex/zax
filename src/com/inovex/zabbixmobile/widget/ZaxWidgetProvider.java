package com.inovex.zabbixmobile.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inovex.zabbixmobile.api.HomescreenWidgetService;

public class ZaxWidgetProvider extends AppWidgetProvider {
	@Override
	public void onDisabled(Context context) {
		// disable Alarm
		Intent refreshIntent = new Intent(context, HomescreenWidgetService.class);
		PendingIntent alarmPendingIntent = PendingIntent.getService(context, 0, refreshIntent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(alarmPendingIntent);
		Log.d("ZaxWidgetProvider", "disabled");
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		// Alarm Manager for refresh
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String minutesStr = prefs.getString("widget_refresh_interval_mins", "15");
		int minutes;
		try {
			minutes = Integer.valueOf(minutesStr);
		} catch (NumberFormatException e) {
			minutes = 15;
		}

		long interval = minutes*60*1000;

		Intent refreshIntent = new Intent(context, HomescreenWidgetService.class);
		PendingIntent alarmPendingIntent = PendingIntent.getService(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long currTime = SystemClock.elapsedRealtime();
		alarmManager.setInexactRepeating(AlarmManager.RTC, currTime+interval, interval, alarmPendingIntent);
		Log.d("ZaxWidgetProvider", "enabled");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// service
		Intent serviceIntent = new Intent(context, HomescreenWidgetService.class);
		context.startService(serviceIntent);
	}
}
