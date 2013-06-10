package com.inovex.zabbixmobile;

import java.sql.SQLException;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.inovex.zabbixmobile.api.ZabbixService;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class ZabbixContentProvider extends ContentProvider {

	protected static final String TAG = ZabbixContentProvider.class
			.getSimpleName();
	private ZabbixService mZabbixService;
	private boolean mBound = false;
	private Context mContext;
	private DatabaseHelper databaseHelper = null;

	public static final String AUTHORITIES = "com.inovex.zabbixmobile.ZabbixContentProvider";
	// URLs
	public static final Uri CONTENT_URI_EVENTS = Uri.parse("content://"
			+ AUTHORITIES + "/events");
	public static final Uri CONTENT_URI_HOSTGROUPS = Uri.parse("content://"
			+ AUTHORITIES + "/hostgroups");
	public static final Uri CONTENT_URI_HOSTS = Uri.parse("content://"
			+ AUTHORITIES + "/hosts");
	public static final Uri CONTENT_URI_APPLICATIONS = Uri.parse("content://"
			+ AUTHORITIES + "/applications");
	public static final Uri CONTENT_URI_ITEMS = Uri.parse("content://"
			+ AUTHORITIES + "/items");
	public static final Uri CONTENT_URI_TRIGGERS = Uri.parse("content://"
			+ AUTHORITIES + "/triggers");
	public static final Uri CONTENT_URI_HISTORY_DETAILS = Uri
			.parse("content://" + AUTHORITIES + "/historydetails");
	public static final Uri CONTENT_URI_SCREENS = Uri.parse("content://"
			+ AUTHORITIES + "/screens");
	public static final Uri CONTENT_URI__FIXTURES = Uri.parse("content://"
			+ AUTHORITIES + "/_fixtures");
	public static final Uri CONTENT_URI__ALL_DATA = Uri.parse("content://"
			+ AUTHORITIES + "/_all");

	private static final int URL_ALL_EVENTS = 1;
	private static final int URL_ALL_HOSTGROUPS = 2;
	private static final int URL_APPLICATIONS_BY_HOST__ID = 3;
	private static final int URL_HOSTS_BY_HOSTGROUP__ID = 4;
	private static final int URL_ITEMS_BY_APPLICATION_AND_HOST__ID = 5;
	private static final int URL_TRIGGERS_BY_HOST__ID = 6;
	private static final int URL_HISTORYDETAILS_BY_ITEM_ID = 7;
	private static final int URL_EVENT_BY_ID = 8;
	private static final int URL_TRIGGER_BY_ID = 9;
	private static final int URL_ITEMS_BY_TRIGGER_ID = 10;
	private static final int URL_EVENT_BY_TRIGGER_ID = 11;
	private static final int URL_ITEM_BY_ID = 12;
	private static final int URL_TRIGGERS_BY_ITEM_ID = 13;
	private static final int URL_ALL_SCREENS = 14;
	private static final int URL_HISTORYDETAILS_BY_SCREEN__ID = 15;
	private static final int URL_ALL_TRIGGERS = 16;
	private static final int URL__FIXTURES = 17;
	private static final int URL__ALL_DATA = 18;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITIES, "events", URL_ALL_EVENTS);
		uriMatcher.addURI(AUTHORITIES, "hostgroups", URL_ALL_HOSTGROUPS);
		uriMatcher.addURI(AUTHORITIES, "hosts/#/applications",
				URL_APPLICATIONS_BY_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "hostgroups/#/hosts",
				URL_HOSTS_BY_HOSTGROUP__ID);
		uriMatcher.addURI(AUTHORITIES, "hosts/#/applications/#/items",
				URL_ITEMS_BY_APPLICATION_AND_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "_fixtures", URL__FIXTURES);
		uriMatcher.addURI(AUTHORITIES, "hosts/#/triggers",
				URL_TRIGGERS_BY_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "_all", URL__ALL_DATA);
		uriMatcher.addURI(AUTHORITIES, "historydetails/#",
				URL_HISTORYDETAILS_BY_ITEM_ID);
		uriMatcher.addURI(AUTHORITIES, "events/#", URL_EVENT_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#", URL_TRIGGER_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#/items",
				URL_ITEMS_BY_TRIGGER_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#/events",
				URL_EVENT_BY_TRIGGER_ID);
		uriMatcher.addURI(AUTHORITIES, "items/#", URL_ITEM_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "items/#/triggers",
				URL_TRIGGERS_BY_ITEM_ID);
		uriMatcher.addURI(AUTHORITIES, "screens", URL_ALL_SCREENS);
		uriMatcher.addURI(AUTHORITIES, "screens/#/graphs/historydetails",
				URL_HISTORYDETAILS_BY_SCREEN__ID);
		uriMatcher.addURI(AUTHORITIES, "triggers", URL_ALL_TRIGGERS);
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder serviceBinder) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ZabbixService.ZabbixBinder binder = (ZabbixService.ZabbixBinder) serviceBinder;
			mZabbixService = binder.getService();
			Log.d(TAG, "service connected");
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "service disconnected");
			mBound = false;
			// we have lost the connection unexpectedly -> try to rebind to
			// service
			Intent intent = new Intent(mContext, ZabbixService.class);
			mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	};

	@Override
	public boolean onCreate() {
		mContext = getContext();
		// set up OrmLite database helper
		getDatabaseHelper();
		// Bind to LocalService
		Intent intent = new Intent(mContext, ZabbixService.class);
		if (!mContext
				.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
			Log.e(TAG, "bindService() not successful.");
			return false;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (uriMatcher.match(uri)) {
		case URL_ALL_EVENTS:
			try {
				Dao<Event, Integer> eventDao = databaseHelper.getDao(Event.class);
//				eventDao.get
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private DatabaseHelper getDatabaseHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(mContext,
					MockDatabaseHelper.class);
		}
		return databaseHelper;
	}

	@Override
	public void shutdown() {
		// Unbind from the service
		if (mBound) {
			mContext.unbindService(mConnection);
			mBound = false;
		}
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

}
