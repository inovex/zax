package com.inovex.zabbixmobile.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.inovex.zabbixmobile.data.HomescreenWidgetService;
import com.inovex.zabbixmobile.model.ZaxPreferences;

/**
 * This class provides the Zax homescreen widget.
 * 
 */
public class ZaxWidgetProvider extends AppWidgetProvider {
	private static final String TAG = ZaxWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");
		// start service
		Intent serviceIntent = new Intent(context,
				HomescreenWidgetService.class);
		context.startService(serviceIntent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(TAG, "onDisabled: deleting alarm");
		stopAlarm(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.d(TAG, "onEnabled");
		// Alarm Manager for refresh
		ZaxPreferences prefs = ZaxPreferences.getInstance(context);
		int minutes = prefs.getWidgetRefreshInterval();
		prefs.setWidgetRefreshIntervalCache(minutes);

		long interval = minutes * 60 * 1000;
		setAlarm(context, interval);
		Log.d(TAG, "alarm set, interval: " + interval + " ms");

	}

	public static void setAlarm(Context context, long updateRate) {
		Intent intent = new Intent(context, WidgetUpdateBroadcastReceiver.class);
		Log.d(TAG, "setting alarm to " + updateRate);
		PendingIntent newPending = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (updateRate > 0) {
			alarms.setRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime() + updateRate, updateRate,
					newPending);
		} else {
			// on a negative updateRate stop the refreshing
			alarms.cancel(newPending);
		}
	}
	
	public static void stopAlarm(Context context) {
		setAlarm(context, -1);
	}

}
