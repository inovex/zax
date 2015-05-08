/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

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
		for (int wid : appWidgetIds) {
			updateView(context, appWidgetManager, wid);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public static void updateView(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Intent serviceIntent = new Intent(context,
				HomescreenWidgetService.class);
		serviceIntent.putExtra(HomescreenWidgetService.WIDGET_ID, appWidgetId);
		context.startService(serviceIntent);
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
