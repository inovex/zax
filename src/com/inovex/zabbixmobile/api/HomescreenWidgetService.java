package com.inovex.zabbixmobile.api;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.inovex.zabbixmobile.GatewayActivity;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.ZabbixContentProvider;
import com.inovex.zabbixmobile.model.TriggerData;
import com.inovex.zabbixmobile.widget.ZaxWidgetProvider;

public class HomescreenWidgetService extends IntentService {
	private enum DisplayStatus {
		ZAX_ERROR(R.drawable.widget_error)
		, OK(R.drawable.widget_ok)
		, AVG(R.drawable.widget_avg)
		, HIGH(R.drawable.widget_high)
		, LOADING(R.drawable.icon);

		private int drawable;
		DisplayStatus(int drawable) { this.drawable = drawable; }
		int getDrawable() { return drawable; }
	}

	private BroadcastReceiver contentProviderReceiver;
	private boolean isError;

	public HomescreenWidgetService() {
		super("HomescreenWidgetService");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		contentProviderReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (intent.getIntExtra("flag", 0)) {
				case ZabbixContentProvider.INTENT_FLAG_CONNECTION_FAILED:
				case ZabbixContentProvider.INTENT_FLAG_AUTH_FAILED:
				case ZabbixContentProvider.INTENT_FLAG_SHOW_EXCEPTION:
				case ZabbixContentProvider.INTENT_FLAG_SSL_NOT_TRUSTED:
					updateView(DisplayStatus.ZAX_ERROR, "ZAX error");
					isError = true;
					break;
				}
			}
		};
		registerReceiver(
			contentProviderReceiver, new IntentFilter(ZabbixContentProvider.CONTENT_PROVIDER_INTENT_ACTION)
		);
	}

	@Override
	public void onDestroy() {
		if (contentProviderReceiver != null) {
			unregisterReceiver(contentProviderReceiver);
		}
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		isError = false;

		// loading view
		updateView(DisplayStatus.LOADING, "loading ...");

		// reset - we reset it always, because we cannot set extraData for the intent.
		// better were it, if we reset the data only if the refresh button was pressed.
		getContentResolver().delete(ZabbixContentProvider.CONTENT_URI__ALL_DATA, null, null);

		// zabbix service
		Cursor cursor = getContentResolver().query(
				ZabbixContentProvider.CONTENT_URI_TRIGGERS
				, null, null, null, null);

		if (cursor == null || isError) {
			updateView(DisplayStatus.ZAX_ERROR, "ZAX error");
		} else {
			int high = 0;
			int avg = 0;

			if (cursor.moveToFirst()) do {
				int severity = cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_PRIORITY));
				int status = cursor.getInt(cursor.getColumnIndex(TriggerData.COLUMN_STATUS));
				if (status == 1) continue; // disabled

				if (severity <= 3) {
					avg++;
				} else if (severity > 3) {
					high++;
				}
			} while (cursor.moveToNext());
			cursor.close();

			if (high == 0 && avg == 0) {
				// ok
				updateView(DisplayStatus.OK, "OK");
			} else {
				// problems
				updateView(high>0?DisplayStatus.HIGH:DisplayStatus.AVG, high+" >=high\n"+avg+" <=avg");
			}
		}
	}

	private void updateView(DisplayStatus icon, String status) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(), ZaxWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.homescreen_widget);

			// Set the text
			remoteViews.setTextViewText(R.id.content, status);

			// set icon
			remoteViews.setImageViewResource(R.id.status_button, icon.getDrawable());

			// status button click
			Intent statusButtonClickIntent = new Intent(this.getApplicationContext(), GatewayActivity.class);
			statusButtonClickIntent.setAction(Intent.ACTION_MAIN);
			statusButtonClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, statusButtonClickIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.status_button, pendingIntent);

			// refresh click
			Intent refreshClickIntent = new Intent(this.getApplicationContext(),
					ZaxWidgetProvider.class);
			refreshClickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			refreshClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
					getApplicationContext(), 1, refreshClickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.refresh_button, pendingIntent2);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

}
