package com.inovex.zabbixmobile.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inovex.zabbixmobile.data.HomescreenWidgetService;
import com.inovex.zabbixmobile.model.ZaxPreferences;

public class WidgetUpdateBroadcastReceiver extends BroadcastReceiver {

	public static final String REFRESH_RATE_CHANGED = "REFRESH_RATE_CHANGED";
	private static final String TAG = WidgetUpdateBroadcastReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(TAG, "received broadcast. Starting service.");
		// check whether the refresh time is still the same
		ZaxPreferences prefs = ZaxPreferences.getInstance(context);

		if (intent.getBooleanExtra(REFRESH_RATE_CHANGED, false)) {
			int widgetRefreshInterval = prefs.getWidgetRefreshInterval();
			ZaxWidgetProvider.stopAlarm(context);
			// refresh rate 0 means no automatic refresh
			if (widgetRefreshInterval > 0)
				ZaxWidgetProvider.setAlarm(context,
						widgetRefreshInterval * 60 * 1000);
		}

		// update all widgets
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName widget1x1 = new ComponentName(context,
				ZaxWidgetProvider.class);
		ComponentName widgetList = new ComponentName(context,
				ZaxWidgetProviderList.class);
		for (int id : appWidgetManager.getAppWidgetIds(widget1x1)) {
			Intent serviceIntent = new Intent(context,
					HomescreenWidgetService.class);
			serviceIntent.putExtra(HomescreenWidgetService.WIDGET_ID, id);
			context.startService(serviceIntent);
		}
		for (int id : appWidgetManager.getAppWidgetIds(widgetList)) {
			Intent serviceIntent = new Intent(context,
					HomescreenWidgetService.class);
			serviceIntent.putExtra(HomescreenWidgetService.WIDGET_ID, id);
			context.startService(serviceIntent);
		}
	}

}
