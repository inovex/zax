package com.inovex.zabbixmobile.widget;

import android.content.BroadcastReceiver;
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
			ZaxWidgetProvider.setAlarm(context, widgetRefreshInterval * 60 * 1000);
		}

		Intent serviceIntent = new Intent(context,
				HomescreenWidgetService.class);
		context.startService(serviceIntent);
	}

}
