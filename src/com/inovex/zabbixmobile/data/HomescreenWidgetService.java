package com.inovex.zabbixmobile.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.widget.ZaxWidgetProvider;
import com.inovex.zabbixmobile.widget.ZaxWidgetProviderList;
import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Started service providing the homescreen widget with functionality to
 * retrieve data from Zabbix (at the moment the active triggers).
 * 
 */
public class HomescreenWidgetService extends Service {
	public static final String WIDGET_ID = "WIDGET_ID";

	private enum DisplayStatus {
		ZAX_ERROR(R.drawable.severity_high), OK(R.drawable.ok), AVG(
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

	private ZabbixRemoteAPI mRemoteAPI;
	private DatabaseHelper mDatabaseHelper;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart");
		final int widgetId = intent.getIntExtra(WIDGET_ID, -1);
		updateView(null, getResources().getString(R.string.widget_loading),
				null, false, widgetId);
		if (mDatabaseHelper == null) {
			// set up SQLite connection using OrmLite
			mDatabaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
		}
		if (mRemoteAPI == null) {
			mRemoteAPI = new ZabbixRemoteAPI(this.getApplicationContext(),
					mDatabaseHelper, null, null);
		}

		RemoteAPITask importProblemsTask = new RemoteAPITask(mRemoteAPI) {

			private List<Trigger> problems;
			private boolean error;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				problems = new ArrayList<Trigger>();
				try {
					mRemoteAPI.authenticate();
					// we need to refresh triggers AND events because otherwise
					// we might lose triggers belonging to events
					mDatabaseHelper.clearEvents();
					mDatabaseHelper.clearTriggers();
					mDatabaseHelper.setNotCached(CacheDataType.EVENT, null);
					mDatabaseHelper.setNotCached(CacheDataType.TRIGGER, null);
					mRemoteAPI.importActiveTriggers(null);
				} catch (FatalException e) {
					error = true;
					return;
				} finally {
					problems = mDatabaseHelper
							.getProblemsBySeverityAndHostGroupId(
									TriggerSeverity.ALL, HostGroup.GROUP_ID_ALL);
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (error) {
					updateView(
							DisplayStatus.ZAX_ERROR.getDrawable(),
							getResources().getString(
									R.string.widget_connection_error),
							problems, false, widgetId);
					stopSelf();
					return;
				}
				if (problems != null) {
					// for (Trigger t : problems)
					// Log.d(TAG, t.toString());
					String status;
					int icon = R.drawable.ok;
					if (problems.size() > 0) {
						int countHigh = 0;
						int countAverage = 0;
						for (Trigger trigger : problems) {
							// make sure disabled triggers are not ignored
							if (trigger.getStatus() != Trigger.STATUS_ENABLED)
								continue;
							if (trigger.getPriority() == TriggerSeverity.DISASTER
									|| trigger.getPriority() == TriggerSeverity.HIGH)
								countHigh++;
							else
								countAverage++;
						}
						if (countHigh > 0)
							icon = DisplayStatus.HIGH.getDrawable();
						else if (countAverage > 0)
							icon = DisplayStatus.AVG.getDrawable();
						else
							icon = DisplayStatus.OK.getDrawable();
						status = problems.get(0).getDescription();
						status = getResources().getString(
								R.string.widget_problems, countHigh,
								countAverage);
					} else {
						icon = DisplayStatus.OK.getDrawable();
						status = getResources().getString(R.string.ok);

					}
					updateView(icon, status, problems, false, widgetId);
				}
			}

		};
		importProblemsTask.execute();

		(new RemoteAPITask(mRemoteAPI) {

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				mRemoteAPI.authenticate();
				mRemoteAPI.importEvents(null);
			}
		}).execute();
		stopSelf();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	private void updateView(Integer statusIcon, String statusText,
			List<Trigger> problems, boolean startProgressSpinner, int widgetId) {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		ComponentName widget1x1 = new ComponentName(getApplicationContext(),
				ZaxWidgetProvider.class);
		ComponentName widgetList = new ComponentName(getApplicationContext(),
				ZaxWidgetProviderList.class);
		List<Integer> widgetIds1x1 = new ArrayList<Integer>();
		for (int id : appWidgetManager.getAppWidgetIds(widget1x1)) {
			widgetIds1x1.add(id);
		}
		List<Integer> widgetIdsList = new ArrayList<Integer>();
		for (int id : appWidgetManager.getAppWidgetIds(widgetList)) {
			widgetIdsList.add(id);
		}

		Log.d(TAG, "widget IDs 1x1: " + widgetIds1x1);

		Log.d(TAG, "widget IDs list: " + widgetIdsList);

		if (widgetIds1x1.size() <= 0 && widgetIdsList.size() <= 0) {
			ZaxWidgetProvider.stopAlarm(getApplicationContext());
			Log.d(TAG, "no widgets added -> stopping alarm");
		}

		if (widgetIds1x1.contains(widgetId)) {
			widgetIds1x1.clear();
			widgetIds1x1.add(widgetId);
			widgetIdsList.clear();
		}
		if (widgetIdsList.contains(widgetId)) {
			widgetIdsList.clear();
			widgetIdsList.add(widgetId);
			widgetIds1x1.clear();
		}

		for (int id : widgetIds1x1) {
			Log.d(TAG, "updating widget. ID: " + id + ", Provider: "
					+ appWidgetManager.getAppWidgetInfo(id).provider.toString());

			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.homescreen_widget_1x1);

			// ZaxPreferences preferences =
			// ZaxPreferences.getInstance(this);
			// remoteViews.setTextViewText(R.id.widget_headline,
			// preferences.getZabbixUrl());

			if (startProgressSpinner) {
				remoteViews.setViewVisibility(R.id.refresh_button, View.GONE);
				remoteViews.setViewVisibility(R.id.refresh_progress,
						View.VISIBLE);
			} else {
				remoteViews
						.setViewVisibility(R.id.refresh_button, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE);
			}

			// Set the text
			remoteViews.setTextViewText(R.id.content, statusText);

			// set icon
			if (statusIcon != null)
				remoteViews
						.setImageViewResource(R.id.status_button, statusIcon);

			// int moreProblems = problems.size() - 1;
			//
			// if (moreProblems == 1)
			// remoteViews.setTextViewText(R.id.widget_more, getResources()
			// .getString(R.string.widget_more_problem, moreProblems));
			// else
			// remoteViews
			// .setTextViewText(
			// R.id.widget_more,
			// getResources().getString(
			// R.string.widget_more_problems,
			// moreProblems));

			// status button click
			Intent statusButtonClickIntent = new Intent(
					this.getApplicationContext(), ProblemsActivity.class);
			statusButtonClickIntent.setAction(Intent.ACTION_MAIN);
			statusButtonClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, statusButtonClickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

			// refresh click
			Intent refreshClickIntent = new Intent(
					this.getApplicationContext(), ZaxWidgetProvider.class);
			refreshClickIntent
					.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			int[] ids = { id };
			refreshClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					ids);

			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
					getApplicationContext(), 1, refreshClickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.refresh_button,
					pendingIntent2);
			appWidgetManager.updateAppWidget(id, remoteViews);
		}

		for (int id : widgetIdsList) {
			Log.d(TAG, "updating widget. ID: " + id + ", Provider: "
					+ appWidgetManager.getAppWidgetInfo(id).provider.toString());

			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.homescreen_widget_list);

			Intent intent = new Intent(getApplicationContext(),
					HomescreenCollectionWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
			if (problems != null) {
				String[] problemTitles = new String[problems.size()];
				for (int i = 0; i < problems.size(); i++) {
					problemTitles[i] = problems.get(i).getDescription();
				}
				intent.putExtra(
						HomescreenCollectionWidgetService.EXTRA_PROBLEMS,
						problemTitles);
			}
			remoteViews.setRemoteAdapter(id, R.id.list_view, intent);

			// remoteViews.setEmptyView(R.id.list_view, R.id.list_view);
			appWidgetManager.updateAppWidget(id, remoteViews);
			appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.list_view);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
