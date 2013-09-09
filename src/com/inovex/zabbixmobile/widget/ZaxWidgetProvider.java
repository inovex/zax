package com.inovex.zabbixmobile.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inovex.zabbixmobile.data.HomescreenWidgetService;

public class ZaxWidgetProvider extends AppWidgetProvider {
	private static final String TAG = ZaxWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// service
		Log.d(TAG, "onUpdate: setting alarm");
		// Alarm Manager for refresh
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String minutesStr = prefs.getString("widget_refresh_interval_mins",
				"15");
		int minutes;
		try {
			minutes = Integer.valueOf(minutesStr);
		} catch (NumberFormatException e) {
			minutes = 15;
		}

		long interval = minutes * 1 * 1000;
		for (int appWidgetId : appWidgetIds) {
			setAlarm(context, appWidgetId, interval);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.d(TAG, "onDeleted: deleting alarm");
		for (int appWidgetId : appWidgetIds) {      
	        setAlarm(context, appWidgetId, -1);
	    }
		super.onDeleted(context, appWidgetIds);
	}

	private static PendingIntent makeControlPendingIntent(Context context,
			String command, int appWidgetId) {
		Intent active = new Intent(context, HomescreenWidgetService.class);
		active.setAction(command);
		active.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// this Uri data is to make the PendingIntent unique, so it wont be
		// updated by FLAG_UPDATE_CURRENT
		// so if there are multiple widget instances they wont override each
		// other
		Uri data = Uri.withAppendedPath(
				Uri.parse("homescreenwidget://widget/id/#" + command
						+ appWidgetId), String.valueOf(appWidgetId));
		active.setData(data);
		return (PendingIntent.getService(context, 0, active,
				PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	private static void setAlarm(Context context, int appWidgetId, long updateRate) {
	    PendingIntent newPending = makeControlPendingIntent(context,HomescreenWidgetService.UPDATE,appWidgetId);
	    AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    if (updateRate >= 0) {
	        alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), updateRate, newPending);
	    } else {
	        // on a negative updateRate stop the refreshing
	        alarms.cancel(newPending);
	    }
	}

}
