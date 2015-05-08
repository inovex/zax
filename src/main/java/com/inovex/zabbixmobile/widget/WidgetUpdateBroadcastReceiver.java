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
