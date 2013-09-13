package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.widget.ZaxWidgetProvider;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class HomescreenWidgetService extends Service {
	private enum DisplayStatus {
		ZAX_ERROR(R.drawable.severity_average), OK(R.drawable.severity_average), AVG(
				R.drawable.severity_average), HIGH(R.drawable.severity_high), LOADING(
				R.drawable.icon);

		private int drawable;

		DisplayStatus(int drawable) {
			this.drawable = drawable;
		}

		int getDrawable() {
			return drawable;
		}
	}

	public static final String UPDATE = "update";
	private static final String TAG = HomescreenWidgetService.class
			.getSimpleName();

	private BroadcastReceiver contentProviderReceiver;
	private boolean isError;

	private ZabbixRemoteAPI mRemoteAPI;
	private DatabaseHelper mDatabaseHelper;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart");
		if (mDatabaseHelper == null) {
			// set up SQLite connection using OrmLite
			mDatabaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
		}
		if (mRemoteAPI == null) {
			mRemoteAPI = new ZabbixRemoteAPI(this.getApplicationContext(),
					mDatabaseHelper, null, null);
		}

		// authenticate
		RemoteAPITask loginTask = new RemoteAPITask(mRemoteAPI) {

			private List<Trigger> problems;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				mRemoteAPI.authenticate();
				problems = new ArrayList<Trigger>();
				try {
					mRemoteAPI.importActiveTriggers(null);
				} finally {
					try {
						problems = mDatabaseHelper
								.getProblemsBySeverityAndHostGroupId(
										TriggerSeverity.ALL,
										HostGroup.GROUP_ID_ALL);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (problems != null) {
					for (Trigger t : problems)
						Log.d(TAG, t.toString());
					updateView(problems);
				}
			}

		};
		loginTask.execute();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		if (contentProviderReceiver != null) {
			unregisterReceiver(contentProviderReceiver);
		}
		super.onDestroy();
	}

	private void updateView(List<Trigger> problems) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				ZaxWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int widgetId : allWidgetIds) {

			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.homescreen_widget);

			ZaxPreferences preferences = new ZaxPreferences(this);
			remoteViews.setTextViewText(R.id.widget_headline,
					preferences.getZabbixUrl());

			String content;
			int icon = R.drawable.widget_ok;
			if (problems.size() > 0) {
				Trigger trigger = problems.get(0);
				switch (trigger.getPriority()) {
				case DISASTER:
				case HIGH:
					icon = DisplayStatus.HIGH.getDrawable();
					break;
				default:
					icon = DisplayStatus.AVG.getDrawable();
				}
				content = problems.get(0).getDescription();

			} else
				content = getResources().getString(R.string.widget_no_problems);

			remoteViews.setImageViewResource(R.id.widget_severity, icon);
			remoteViews.setTextViewText(R.id.widget_content, content);

			int moreProblems = problems.size() - 1;

			if (moreProblems == 1)
				remoteViews.setTextViewText(R.id.widget_more, getResources()
						.getString(R.string.widget_more_problem, moreProblems));
			else
				remoteViews
						.setTextViewText(
								R.id.widget_more,
								getResources().getString(
										R.string.widget_more_problems,
										moreProblems));

			// status button click
			Intent statusButtonClickIntent = new Intent(
					this.getApplicationContext(), ProblemsActivity.class);
			statusButtonClickIntent.setAction(Intent.ACTION_MAIN);
			statusButtonClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, statusButtonClickIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_content_layout,
					pendingIntent);
			remoteViews
					.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);

			// refresh click
			Intent refreshClickIntent = new Intent(
					this.getApplicationContext(), ZaxWidgetProvider.class);
			refreshClickIntent
					.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			refreshClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
					getApplicationContext(), 1, refreshClickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget_refresh,
					pendingIntent2);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
