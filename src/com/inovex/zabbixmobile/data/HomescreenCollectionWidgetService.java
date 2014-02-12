package com.inovex.zabbixmobile.data;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.ProblemsActivity;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.j256.ormlite.android.apptools.OpenHelperManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HomescreenCollectionWidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new CollectionRemoteViewsFactory(this.getApplicationContext(),
				intent);
	}

	class CollectionRemoteViewsFactory implements RemoteViewsFactory {

		private Context mContext;
		private int mAppWidgetId;
		private List<Trigger> mWidgetItems = new ArrayList<Trigger>();
		private DatabaseHelper mDatabaseHelper;

		public CollectionRemoteViewsFactory(Context context, Intent intent) {
			mContext = context;
			mAppWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		@Override
		public void onCreate() {
			// set up database connection to be able to query the problems later
			if (mDatabaseHelper == null) {
				// set up SQLite connection using OrmLite
				mDatabaseHelper = OpenHelperManager.getHelper(
						getApplicationContext(), DatabaseHelper.class);
			}
		}

		@Override
		public void onDataSetChanged() {
			mWidgetItems = mDatabaseHelper.getProblemsBySeverityAndHostGroupId(
					TriggerSeverity.ALL, HostGroup.GROUP_ID_ALL);
		}

		@Override
		public void onDestroy() {
			if (mDatabaseHelper != null)
				mDatabaseHelper.close();
		}

		@Override
		public int getCount() {
			return mWidgetItems.size();
		}

		@Override
		public RemoteViews getViewAt(int position) {
			RemoteViews rv = new RemoteViews(mContext.getPackageName(),
					R.layout.homescreen_widget_list_item);
			Trigger trigger = mWidgetItems.get(position);
			rv.setImageViewResource(R.id.status, trigger.getPriority()
					.getImageResourceId());
			rv.setTextViewText(R.id.host, trigger.getHostNames());
			rv.setTextViewText(R.id.description, trigger.getDescription());
			
			// on click intent
			Intent fillInIntent = new Intent();
			fillInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			fillInIntent.putExtra(ProblemsActivity.ARG_TRIGGER_POSITION, position);
			rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

			return rv;
		}

		@Override
		public RemoteViews getLoadingView() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

	}
}
