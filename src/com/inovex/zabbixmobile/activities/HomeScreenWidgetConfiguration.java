package com.inovex.zabbixmobile.activities;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ListView;

import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.widget.ZaxWidgetProvider;

public class HomeScreenWidgetConfiguration extends ListActivity implements
		ServiceConnection {
	private int mAppWidgetId;
	private ZabbixDataService mZabbixDataService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		bindService();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ZabbixServer srv = mZabbixDataService.getServersListManagementAdapter().getItem(position);

		ZaxPreferences.getInstance(getApplicationContext()).setWidgetServer(mAppWidgetId, srv.getId());

		// Push widget update to surface with newly set prefix
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		ZaxWidgetProvider.updateView(getApplicationContext(), appWidgetManager,
				mAppWidgetId);

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		boolean useMockData = getIntent().getBooleanExtra(
				ZabbixDataService.EXTRA_IS_TESTING, false);
		intent.putExtra(ZabbixDataService.EXTRA_IS_TESTING, useMockData);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
		mZabbixDataService.setActivityContext(this);
		mZabbixDataService.loadZabbixServers(); // sync

		setListAdapter(mZabbixDataService.getServersListManagementAdapter());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixDataService = null;
	}

	@Override
	protected void onDestroy() {
		getApplicationContext().unbindService(this);
		super.onDestroy();
	}
}
