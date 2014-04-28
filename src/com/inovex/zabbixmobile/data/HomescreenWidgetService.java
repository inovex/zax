package com.inovex.zabbixmobile.data;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Cache.CacheDataType;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.model.ZaxPreferences;
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

		private final int drawable;

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

	private DatabaseHelper mDatabaseHelper;
	private RemoteAPITask importProblemsTask;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart");
		final int widgetId = intent.getIntExtra(WIDGET_ID, -1);
		if (widgetId == -1) {
			stopSelf();
			return;
		}
		long zabbixServerId = ZaxPreferences.getInstance(getApplicationContext()).getWidgetServer(widgetId);

		updateView(null, getResources().getString(R.string.widget_loading),
				true, widgetId);
		if (zabbixServerId == -1) {
			updateView(
					DisplayStatus.ZAX_ERROR.getDrawable(),
					getResources().getString(
							R.string.widget_connection_error), false,
					widgetId);
			stopSelf();
			return;
		}
		if (mDatabaseHelper == null) {
			// set up SQLite connection using OrmLite
			mDatabaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
		}

		final ZabbixRemoteAPI mRemoteAPI = new ZabbixRemoteAPI(this.getApplicationContext(),
					mDatabaseHelper, zabbixServerId, null, null);

		if (importProblemsTask != null && importProblemsTask.getStatus() == AsyncTask.Status.RUNNING && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// your android version is too old
			Toast.makeText(getApplicationContext(), R.string.android_version_too_old_for_parallel, Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		}

		importProblemsTask = new RemoteAPITask(mRemoteAPI) {

			private List<Trigger> problems;
			private boolean error;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				problems = new ArrayList<Trigger>();
				try {
					if (!mRemoteAPI.isLoggedIn()) {
						mRemoteAPI.authenticate();
					}
					// ensure that hosts and host groups are available; if they
					// are cached, no API call will be triggered.
					mRemoteAPI.importHostsAndGroups();
					mDatabaseHelper.clearTriggers();
					// we need to refresh triggers AND events because otherwise
					// we might lose triggers belonging to events. But we do not
					// want to do that here, but set them to "not cached" so
					// they will be refreshed on demand
					mDatabaseHelper.setNotCached(CacheDataType.EVENT, null);
					mDatabaseHelper.setNotCached(CacheDataType.TRIGGER, null);
					mRemoteAPI.importActiveTriggers(null);
				} catch (Exception e) {
					error = true;
					e.printStackTrace();
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
									R.string.widget_connection_error), false,
							widgetId);
					importProblemsTask = null;
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
					updateView(icon, status, false, widgetId);
					importProblemsTask = null;
					stopSelf();
				}
			}
		};
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			importProblemsTask.execute();
		} else {
			executeTask();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeTask() {
		importProblemsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	private void updateView(Integer statusIcon, String statusText,
			boolean startProgressSpinner, int widgetId) {
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
			Log.d(TAG, "updating widget. ID: " + widgetId + ", Provider: "
					+ appWidgetManager.getAppWidgetInfo(widgetId).provider.toString());

			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.homescreen_widget_1x1);

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
				remoteViews.setImageViewResource(R.id.status_image, statusIcon);

			// widget click
			Intent statusButtonClickIntent = new Intent(
					this.getApplicationContext(), ProblemsActivity.class);
			statusButtonClickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			statusButtonClickIntent.setAction(Intent.ACTION_MAIN);
			statusButtonClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 123456, statusButtonClickIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

			// refresh click
			Intent refreshClickIntent = new Intent(
					this.getApplicationContext(), ZaxWidgetProvider.class);
			refreshClickIntent
					.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			int[] ids = { widgetId };
			refreshClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					ids);
			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
					getApplicationContext(), widgetId, refreshClickIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.refresh_button,
					pendingIntent2);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

		if (widgetIdsList.contains(widgetId)) {
			Log.d(TAG, "updating widget. ID: " + widgetId + ", Provider: "
					+ appWidgetManager.getAppWidgetInfo(widgetId).provider.toString());
			// appWidgetManager.notifyAppWidgetViewDataChanged(id,
			// R.id.list_view);
			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.homescreen_widget_list);

			if (startProgressSpinner) {
				remoteViews.setViewVisibility(R.id.refresh_button, View.GONE);
				remoteViews.setViewVisibility(R.id.refresh_progress,
						View.VISIBLE);
			} else {
				remoteViews
						.setViewVisibility(R.id.refresh_button, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.refresh_progress, View.GONE);
			}

			// widget click
			Intent statusButtonClickIntent = new Intent(
					this.getApplicationContext(), ProblemsActivity.class);
			statusButtonClickIntent.setAction(Intent.ACTION_MAIN);
			statusButtonClickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			statusButtonClickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 12345, statusButtonClickIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

			// refresh click
			Intent refreshClickIntent = new Intent(
					this.getApplicationContext(), ZaxWidgetProvider.class);
			refreshClickIntent
					.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			int[] ids = { widgetId };
			refreshClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					ids);
			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
					getApplicationContext(), widgetId, refreshClickIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.refresh_button,
					pendingIntent2);

			// set up item click intents
			Intent itemIntent = new Intent(getApplicationContext(),
					ProblemsActivity.class);
			PendingIntent itemPendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, itemIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			remoteViews.setPendingIntentTemplate(R.id.list_view,
					itemPendingIntent);

			boolean isError = statusText.equals(getResources().getString(
					R.string.widget_connection_error));
			if (isError) {
				remoteViews.setViewVisibility(R.id.list_view, View.GONE);
				remoteViews.setViewVisibility(R.id.error_view, View.VISIBLE);

			} else if (!startProgressSpinner) {
				remoteViews.setViewVisibility(R.id.list_view, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.error_view, View.GONE);
				// empty view
				remoteViews.setEmptyView(R.id.list_view, R.id.empty_view);

				// fill list
				Intent intent = new Intent(getApplicationContext(),
						HomescreenCollectionWidgetService.class);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
				remoteViews.setRemoteAdapter(widgetId, R.id.list_view, intent);

				appWidgetManager.notifyAppWidgetViewDataChanged(widgetId,
						R.id.list_view);
			}
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
