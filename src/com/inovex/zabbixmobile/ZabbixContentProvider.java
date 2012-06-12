package com.inovex.zabbixmobile;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.inovex.zabbixmobile.api.ZabbixService;
import com.inovex.zabbixmobile.api.ZabbixService.HttpAuthorizationRequiredException;
import com.inovex.zabbixmobile.api.ZabbixService.NoAPIAccessException;
import com.inovex.zabbixmobile.api.ZabbixService.PreconditionFailedException;
import com.inovex.zabbixmobile.api.ZabbixService.ZabbixConfig;
import com.inovex.zabbixmobile.model.ApplicationData;
import com.inovex.zabbixmobile.model.ApplicationItemRelationData;
import com.inovex.zabbixmobile.model.BaseModelData;
import com.inovex.zabbixmobile.model.CacheData;
import com.inovex.zabbixmobile.model.DatabaseFixtures;
import com.inovex.zabbixmobile.model.EventData;
import com.inovex.zabbixmobile.model.GraphData;
import com.inovex.zabbixmobile.model.GraphItemData;
import com.inovex.zabbixmobile.model.HistoryDetailData;
import com.inovex.zabbixmobile.model.HostData;
import com.inovex.zabbixmobile.model.HostGroupData;
import com.inovex.zabbixmobile.model.ItemData;
import com.inovex.zabbixmobile.model.ScreenData;
import com.inovex.zabbixmobile.model.ScreenItemData;
import com.inovex.zabbixmobile.model.TriggerData;

/**
 * interface between gui and zabbix service.
 * zabbix data will be stored in sqlite db.
 */
public class ZabbixContentProvider extends ContentProvider {
	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, "com.inovex.zabbixmobile.db", null, 63);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * caution:
			 * new tables has to be created at #onCreate #onUpgrade or #delete
			 */

			// create new tables
			EventData.create(db);
			TriggerData.create(db);
			HostData.create(db);
			HostGroupData.create(db);
			ItemData.create(db);
			ApplicationData.create(db);
			CacheData.create(db);
			ApplicationItemRelationData.create(db);
			HistoryDetailData.create(db);
			ScreenData.create(db);
			ScreenItemData.create(db);
			GraphData.create(db);
			GraphItemData.create(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// delete all tables
			EventData.drop(db);
			TriggerData.drop(db);
			HostData.drop(db);
			HostGroupData.drop(db);
			ItemData.drop(db);
			ApplicationData.drop(db);
			CacheData.drop(db);
			ApplicationItemRelationData.drop(db);
			HistoryDetailData.drop(db);
			ScreenData.drop(db);
			ScreenItemData.drop(db);
			GraphData.drop(db);
			GraphItemData.drop(db);

			onCreate(db);
		}
	}

	public static final int INTENT_FLAG_AUTH_FAILED = 1;
	public static final int INTENT_FLAG_CONNECTION_FAILED = 2;
	public static final int INTENT_FLAG_SHOW_PROGRESS = 3;
	public static final int INTENT_FLAG_SHOW_EXCEPTION = 4;
	public static final int INTENT_FLAG_SSL_NOT_TRUSTED = 5;
	public static final int INTENT_FLAG_AUTH_SUCCESSFUL = 6;
	public static final String CONTENT_PROVIDER_INTENT_ACTION = "com.inovex.zabbixmobile.action.INFO";

	public static final String AUTHORITIES = "com.inovex.zabbixmobile.ZabbixContentProvider";
	// URLs
	public static final Uri CONTENT_URI_EVENTS = Uri.parse("content://"+AUTHORITIES+"/events");
	public static final Uri CONTENT_URI_HOSTGROUPS = Uri.parse("content://"+AUTHORITIES+"/hostgroups");
	public static final Uri CONTENT_URI_HOSTS = Uri.parse("content://"+AUTHORITIES+"/hosts");
	public static final Uri CONTENT_URI_APPLICATIONS = Uri.parse("content://"+AUTHORITIES+"/applications");
	public static final Uri CONTENT_URI_ITEMS = Uri.parse("content://"+AUTHORITIES+"/items");
	public static final Uri CONTENT_URI_TRIGGERS = Uri.parse("content://"+AUTHORITIES+"/triggers");
	public static final Uri CONTENT_URI_HISTORY_DETAILS = Uri.parse("content://"+AUTHORITIES+"/historydetails");
	public static final Uri CONTENT_URI_SCREENS = Uri.parse("content://"+AUTHORITIES+"/screens");
	public static final Uri CONTENT_URI__FIXTURES = Uri.parse("content://"+AUTHORITIES+"/_fixtures");
	public static final Uri CONTENT_URI__ALL_DATA = Uri.parse("content://"+AUTHORITIES+"/_all");

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
		uriMatcher.addURI(AUTHORITIES, "hosts/#/applications", URL_APPLICATIONS_BY_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "hostgroups/#/hosts", URL_HOSTS_BY_HOSTGROUP__ID);
		uriMatcher.addURI(AUTHORITIES, "hosts/#/applications/#/items", URL_ITEMS_BY_APPLICATION_AND_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "_fixtures", URL__FIXTURES);
		uriMatcher.addURI(AUTHORITIES, "hosts/#/triggers", URL_TRIGGERS_BY_HOST__ID);
		uriMatcher.addURI(AUTHORITIES, "_all", URL__ALL_DATA);
		uriMatcher.addURI(AUTHORITIES, "historydetails/#", URL_HISTORYDETAILS_BY_ITEM_ID);
		uriMatcher.addURI(AUTHORITIES, "events/#", URL_EVENT_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#", URL_TRIGGER_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#/items", URL_ITEMS_BY_TRIGGER_ID);
		uriMatcher.addURI(AUTHORITIES, "triggers/#/events", URL_EVENT_BY_TRIGGER_ID);
		uriMatcher.addURI(AUTHORITIES, "items/#", URL_ITEM_BY_ID);
		uriMatcher.addURI(AUTHORITIES, "items/#/triggers", URL_TRIGGERS_BY_ITEM_ID);
		uriMatcher.addURI(AUTHORITIES, "screens", URL_ALL_SCREENS);
		uriMatcher.addURI(AUTHORITIES, "screens/#/graphs/historydetails", URL_HISTORYDETAILS_BY_SCREEN__ID);
		uriMatcher.addURI(AUTHORITIES, "triggers", URL_ALL_TRIGGERS);
	}

	private SQLiteDatabase zabbixLocalDB;
	private ZabbixService zabbix;
	private SQLiteOpenHelper dbHelper;
	private boolean authError;

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		if (uri.equals(CONTENT_URI__ALL_DATA)) {
			// do a reset
			int n=0;
			for (String kind : new String[] {
					ApplicationData.TABLE_NAME,
					ApplicationItemRelationData.TABLE_NAME,
					CacheData.TABLE_NAME,
					EventData.TABLE_NAME,
					HistoryDetailData.TABLE_NAME,
					HostData.TABLE_NAME,
					HostGroupData.TABLE_NAME,
					ItemData.TABLE_NAME,
					TriggerData.TABLE_NAME,
					ScreenData.TABLE_NAME,
					ScreenItemData.TABLE_NAME,
					GraphData.TABLE_NAME,
					GraphItemData.TABLE_NAME
			}) {
				n += zabbixLocalDB.delete(kind, arg1, arg2);
			}
			zabbix = null; // force new auth
			authError = false;
			return n;
		} else if (uri.equals(CONTENT_URI_EVENTS)) {
			return zabbixLocalDB.delete(EventData.TABLE_NAME, arg1, arg2);
		} else if (uri.equals(CONTENT_URI_HOSTS)) {
			return zabbixLocalDB.delete(HostData.TABLE_NAME, arg1, arg2);
		} else if (uri.equals(CONTENT_URI_HOSTGROUPS)) {
			return zabbixLocalDB.delete(HostGroupData.TABLE_NAME, arg1, arg2);
		} else if (uri.equals(CONTENT_URI_ITEMS)) {
			return zabbixLocalDB.delete(ItemData.TABLE_NAME, arg1, arg2);
		} else if (uri.equals(CONTENT_URI_TRIGGERS)) {
			int r = zabbixLocalDB.delete(TriggerData.TABLE_NAME, arg1, arg2);
			getContext().getContentResolver().notifyChange(CONTENT_URI_TRIGGERS, null);
			return r;
		}
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	/**
	 * only for unit test
	 * @return
	 */
	public SQLiteDatabase getZabbixLocalDB() {
		return zabbixLocalDB;
	}

	/**
	 * handle exceptions: log, free resources and notify the gui
	 * @param e1
	 */
	private void handleServiceException(Exception e1) {
		// logcat
		e1.printStackTrace();

		// free resources
		try {
			zabbix.closeLastStream();
			zabbix._endTransaction();
		} catch (Exception e) {
		}

		// no further queries
		authError = true;

		// activity
		Intent intent = new Intent(CONTENT_PROVIDER_INTENT_ACTION);
		if (e1 instanceof NoAPIAccessException) {
			intent.putExtra("flag", INTENT_FLAG_AUTH_FAILED);
			intent.putExtra("noApiAccess", true);
		} else if (e1 instanceof SSLPeerUnverifiedException) {
			// SSL not trusted
			intent.putExtra("flag", ZabbixContentProvider.INTENT_FLAG_SSL_NOT_TRUSTED);
		} else {
			intent.putExtra("flag", ZabbixContentProvider.INTENT_FLAG_SHOW_EXCEPTION);
			intent.putExtra("value", e1.toString()+"/"+e1.getMessage());
		}
		getContext().sendBroadcast(intent);
	}

	/**
	 * if there's no zabbix service, it will be created and auth
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	synchronized private void initZabbixService() throws ClientProtocolException, IOException, JSONException {
		if (zabbix == null) {
			zabbix = new ZabbixService(getContext(), zabbixLocalDB);
			boolean auth = false;
			boolean httpAuthRequired = false;
			boolean noApiAccess = false;
			boolean preconditionFailed = false;

			try {
				auth = zabbix.authenticate();
			} catch (java.lang.UnsupportedOperationException e1) {
				// for unit test
				auth = true;
			} catch (NoAPIAccessException e) {
				noApiAccess = true;
			} catch (HttpAuthorizationRequiredException e) {
				httpAuthRequired = true;
			} catch (PreconditionFailedException e) {
				preconditionFailed = true;
			} catch (IOException e) {
				zabbix = null;
				authError = true;
				throw e; // send to gui
			}
			if (!auth) {
				zabbix = null;
				Intent intent = new Intent(CONTENT_PROVIDER_INTENT_ACTION);
				intent.putExtra("flag", INTENT_FLAG_AUTH_FAILED);
				intent.putExtra("httpAuthRequired", httpAuthRequired);
				intent.putExtra("noApiAccess", noApiAccess);
				intent.putExtra("preconditionFailed", preconditionFailed);
				getContext().sendBroadcast(intent);
				throw new IllegalStateException();
			}

			// all fine
			Intent intent = new Intent(CONTENT_PROVIDER_INTENT_ACTION);
			intent.putExtra("flag", INTENT_FLAG_AUTH_SUCCESSFUL);
			getContext().sendBroadcast(intent);
		}
	}

	/**
	 * unit test, to insert fixtures
	 */
	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		if (arg0.equals(CONTENT_URI__FIXTURES)) {
			delete(CONTENT_URI__ALL_DATA, null, null);
			DatabaseFixtures.insert(zabbixLocalDB);
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		zabbixLocalDB = dbHelper.getWritableDatabase();
		return (zabbixLocalDB == null)? false:true;
	}

	@Override
	synchronized public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3, String arg4) {
		if (authError) {
			return null;
		}

		try {
			initZabbixService();
		} catch (IllegalStateException e1) {
			// exception was already sent to gui
			return null;
		} catch (Exception e1) {
			handleServiceException(e1);
			return null;
		}
		SQLiteQueryBuilder sqlBuilder;

		switch (uriMatcher.match(uri)) {
		case URL_ALL_EVENTS:
			try {
				zabbix.importEvents();
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(EventData.TABLE_NAME+" LEFT OUTER JOIN "+TriggerData.TABLE_NAME+" ON ("+EventData.TABLE_NAME+"."+EventData.COLUMN_OBJECTID+"="+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_TRIGGERID+")");
			return sqlBuilder.query(
				zabbixLocalDB,
				new String[] {
						EventData.TABLE_NAME+"."+EventData.COLUMN_CLOCK
						, EventData.TABLE_NAME+"."+EventData.COLUMN_EVENTID
						, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_TRIGGERID
						, EventData.TABLE_NAME+"."+EventData.COLUMN_HOSTS
						, EventData.TABLE_NAME+"."+EventData.COLUMN_VALUE
						, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_DESCRIPTION
						, EventData.TABLE_NAME+"."+BaseModelData.COLUMN__ID
				},
				null,
				null,
				null,
				null,
				EventData.COLUMN_CLOCK+" DESC"
			);
		case URL_ALL_TRIGGERS: {
			// Trigger problems. used for homescreen widget
			try {
				zabbix.importTriggers();
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(TriggerData.TABLE_NAME);

			long min = (new Date().getTime()/1000)-ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;
			sqlBuilder.appendWhere(
				TriggerData.COLUMN_LASTCHANGE+">"+min
				+ " AND "+TriggerData.COLUMN_VALUE+"=1");
			return sqlBuilder.query(
				zabbixLocalDB,
				new String[] {
						TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_STATUS
						, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_PRIORITY
						, TriggerData.TABLE_NAME+"."+BaseModelData.COLUMN__ID
						, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_ITEMID
				},
				null,
				null,
				null,
				null,
				null
			);
		}
		case URL_ALL_HOSTGROUPS: {
			try {
				zabbix.importHostsAndGroups();
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			String groupBy;
			String[] selection;
			if (arg3 != null && arg3[0].equals("triggerFlag")) {
				try {
					zabbix.importTriggers();
				} catch (Exception e) {
					handleServiceException(e);
				}

				sqlBuilder.setTables(HostGroupData.TABLE_NAME+" inner join "+HostData.TABLE_NAME+" on "+HostData.TABLE_NAME+"."+HostData.COLUMN_GROUPID+"="+HostGroupData.TABLE_NAME+"."+HostGroupData.COLUMN_GROUPID+" inner join "+TriggerData.TABLE_NAME+" on "+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID+"="+HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID);
				groupBy = HostGroupData.TABLE_NAME+"."+HostGroupData.COLUMN_GROUPID;
				selection = new String[] {
						HostGroupData.TABLE_NAME+"."+BaseModelData.COLUMN__ID
						, HostGroupData.TABLE_NAME+"."+HostGroupData.COLUMN_NAME
						, "max("+TriggerData.COLUMN_PRIORITY+") AS "+TriggerData.COLUMN_PRIORITY
				};
				long min = (new Date().getTime()/1000)-ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;
				sqlBuilder.appendWhere(
						TriggerData.COLUMN_LASTCHANGE+">"+min
						+ " AND "+TriggerData.COLUMN_VALUE+"=1"
						+ " AND "+TriggerData.COLUMN_STATUS+"=0"); // only enabled triggers
			} else {
				sqlBuilder.setTables(HostGroupData.TABLE_NAME);
				groupBy = null;
				selection = null;
			}
			Cursor cursor = sqlBuilder.query(
				zabbixLocalDB,
				selection,
				null,
				null,
				groupBy,
				null,
				HostGroupData.COLUMN_NAME
			);
			if (selection != null) {
				cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_TRIGGERS);
			} else {
				cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_HOSTGROUPS);
			}
			return cursor;
		}
		case URL_APPLICATIONS_BY_HOST__ID: {
			String host__id = uri.getPathSegments().get(1);
			// find out zabbix hostid
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(HostData.TABLE_NAME);
			sqlBuilder.appendWhere(HostData.COLUMN__ID+"="+host__id);
			Cursor hostCursor = sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				null,
				null,
				null
			);
			hostCursor.moveToFirst();
			long hostid = hostCursor.getLong(hostCursor.getColumnIndex(HostData.COLUMN_HOSTID));
			hostCursor.close();

			try {
				zabbix.importItems(hostid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables("("+ItemData.TABLE_NAME+" INNER JOIN "+ApplicationItemRelationData.TABLE_NAME+" ON "+ApplicationItemRelationData.TABLE_NAME+"."+ApplicationItemRelationData.COLUMN_ITEMID+"="+ItemData.TABLE_NAME+"."+ItemData.COLUMN_ITEMID+") INNER JOIN "+ApplicationData.TABLE_NAME+" ON "+ApplicationData.TABLE_NAME+"."+ApplicationData.COLUMN_APPLICATIONID+"="+ApplicationItemRelationData.TABLE_NAME+"."+ApplicationItemRelationData.COLUMN_APPLICATIONID);
			sqlBuilder.appendWhere(ItemData.TABLE_NAME+"."+ItemData.COLUMN_HOSTID+"="+hostid);
			return sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				ApplicationData.TABLE_NAME+"."+ApplicationData.COLUMN_APPLICATIONID,
				null,
				ApplicationData.COLUMN_NAME
			);
		}
		case URL_HOSTS_BY_HOSTGROUP__ID: {
			String group__id = uri.getPathSegments().get(1);
			// find out zabbix groupid
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(HostGroupData.TABLE_NAME);
			sqlBuilder.appendWhere(HostGroupData.COLUMN__ID+"="+group__id);
			Cursor groupCursor = sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				null,
				null,
				null
			);
			groupCursor.moveToFirst();
			long groupid = groupCursor.getLong(groupCursor.getColumnIndex(HostGroupData.COLUMN_GROUPID));
			groupCursor.close();

			sqlBuilder = new SQLiteQueryBuilder();
			String groupBy;
			String[] selection;
			if (arg3 != null && arg3[0].equals("triggerFlag")) {
				sqlBuilder.setTables(HostData.TABLE_NAME+" inner join "+TriggerData.TABLE_NAME+" on "+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID+"="+HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID);
				groupBy = TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID;
				selection = new String[] {
						HostData.TABLE_NAME+"."+BaseModelData.COLUMN__ID
						, HostData.TABLE_NAME+"."+HostData.COLUMN_HOST
						, HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID
						, "max("+TriggerData.COLUMN_PRIORITY+") AS "+TriggerData.COLUMN_PRIORITY
				};
				long min = (new Date().getTime()/1000)-ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;
				sqlBuilder.appendWhere(
						TriggerData.COLUMN_LASTCHANGE+">"+min
						+ " AND "+TriggerData.COLUMN_VALUE+"=1"
						+ " AND "+TriggerData.COLUMN_STATUS+"=0 AND ");
			} else {
				sqlBuilder.setTables(HostData.TABLE_NAME);
				groupBy = null;
				selection = null;
			}
			sqlBuilder.appendWhere(HostData.COLUMN_GROUPID+"="+groupid);
			return sqlBuilder.query(
				zabbixLocalDB,
				selection,
				null,
				null,
				groupBy,
				null,
				HostData.COLUMN_HOST
			);
		}
		case URL_ITEMS_BY_APPLICATION_AND_HOST__ID: {
				String host__id = uri.getPathSegments().get(1);
				// find out zabbix hostid
				sqlBuilder = new SQLiteQueryBuilder();
				sqlBuilder.setTables(HostData.TABLE_NAME);
				sqlBuilder.appendWhere(HostData.COLUMN__ID+"="+host__id);
				Cursor hostCursor = sqlBuilder.query(
					zabbixLocalDB,
					null,
					null,
					null,
					null,
					null,
					null
				);
				hostCursor.moveToFirst();
				long hostid = hostCursor.getLong(hostCursor.getColumnIndex(HostData.COLUMN_HOSTID));
				hostCursor.close();
				String applicationid = uri.getPathSegments().get(3);

				// at this point it is not possible to import something because the quire is always syncron here.

				sqlBuilder = new SQLiteQueryBuilder();
				sqlBuilder.setTables(ItemData.TABLE_NAME+" LEFT OUTER JOIN "+ApplicationItemRelationData.TABLE_NAME+" ON ("+ItemData.TABLE_NAME+"."+ItemData.COLUMN_ITEMID+"="+ApplicationItemRelationData.TABLE_NAME+"."+ApplicationItemRelationData.COLUMN_ITEMID+")");
				sqlBuilder.appendWhere(ApplicationItemRelationData.TABLE_NAME+"."+ApplicationItemRelationData.COLUMN_APPLICATIONID+"="+applicationid);
				sqlBuilder.appendWhere(" AND "+ItemData.TABLE_NAME+"."+ItemData.COLUMN_HOSTID+"="+hostid);
				return sqlBuilder.query(
						zabbixLocalDB,
						null,
						null,
						null,
						null,
						null,
						ItemData.COLUMN_DESCRIPTION
					);
			}
		case URL_TRIGGERS_BY_HOST__ID: {
			String host__id = uri.getPathSegments().get(1);
			// find out zabbix hostid
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(HostData.TABLE_NAME);
			sqlBuilder.appendWhere(HostData.COLUMN__ID+"="+host__id);
			Cursor hostCursor = sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				null,
				null,
				null
			);
			hostCursor.moveToFirst();
			long hostid = hostCursor.getLong(hostCursor.getColumnIndex(HostData.COLUMN_HOSTID));
			hostCursor.close();

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(TriggerData.TABLE_NAME+" inner join "+HostData.TABLE_NAME+" on "+HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID+"="+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID);
			sqlBuilder.appendWhere(TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID+"="+hostid);
			long min = (new Date().getTime()/1000)-ZabbixConfig.STATUS_SHOW_TRIGGER_TIME;
			sqlBuilder.appendWhere(
					" AND "+TriggerData.COLUMN_LASTCHANGE+">"+min
					+ " AND "+TriggerData.COLUMN_VALUE+"=1"
					+ " AND "+TriggerData.COLUMN_STATUS+"=0");
			return sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				null,
				null,
				TriggerData.COLUMN_LASTCHANGE+" DESC"
			);
		}
		case URL_HISTORYDETAILS_BY_ITEM_ID: {
			String itemid = uri.getPathSegments().get(1);

			try {
				zabbix.importHistoryDetails(itemid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(HistoryDetailData.TABLE_NAME);
			// the past 2 hours
			long time_till = new Date().getTime() / 1000;
			long time_from = time_till - ZabbixConfig.HISTORY_GET_TIME_FROM_SHIFT;
			sqlBuilder.appendWhere(HistoryDetailData.COLUMN_ITEMID+"="+itemid+" AND "+HistoryDetailData.COLUMN_CLOCK+">"+time_from+" AND "+HistoryDetailData.COLUMN_CLOCK+"<"+time_till);
			return sqlBuilder.query(
				zabbixLocalDB,
				null,
				null,
				null,
				null,
				null,
				HistoryDetailData.COLUMN_CLOCK
			);
		}
		case URL_EVENT_BY_ID: {
			String event__id = uri.getPathSegments().get(1);
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(EventData.TABLE_NAME);
			sqlBuilder.appendWhere(EventData.COLUMN_EVENTID+"="+event__id);
			return sqlBuilder.query(zabbixLocalDB , null, null, null, null, null, null);
		}
		case URL_TRIGGER_BY_ID: {
			String triggerid = uri.getPathSegments().get(1);

			// do a join on hosts, so that "hosts" is always set
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(TriggerData.TABLE_NAME+" inner join "+HostData.TABLE_NAME+" on "+HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID+"="+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_HOSTID);
			sqlBuilder.appendWhere(TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_TRIGGERID+"="+triggerid);
			return sqlBuilder.query(
					zabbixLocalDB
					, new String[] {
							TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_DESCRIPTION
							, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_PRIORITY
							, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_EXPRESSION
							, TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_STATUS
							, HostData.TABLE_NAME+"."+HostData.COLUMN_HOST
					}
					, null, null, null, null, null);
		}
		case URL_TRIGGERS_BY_ITEM_ID: {
			String itemid = uri.getPathSegments().get(1);

			try {
				zabbix.importTriggersByItemId(itemid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(TriggerData.TABLE_NAME);
			sqlBuilder.appendWhere(TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_ITEMID+"="+itemid);
			return sqlBuilder.query(
					zabbixLocalDB
					, null
					, null, null, null, null, TriggerData.COLUMN_LASTCHANGE+" DESC");
		}
		case URL_ITEMS_BY_TRIGGER_ID: {
			String triggerid = uri.getPathSegments().get(1);

			try {
				zabbix.importTriggerColumnItemId(triggerid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(ItemData.TABLE_NAME+" inner join "+TriggerData.TABLE_NAME+" on "+TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_ITEMID+"="+ItemData.TABLE_NAME+"."+ItemData.COLUMN_ITEMID);
			sqlBuilder.appendWhere(TriggerData.COLUMN_TRIGGERID+"="+triggerid);
			return sqlBuilder.query(
					zabbixLocalDB
					, new String[] {
							ItemData.COLUMN_LASTVALUE
							, ItemData.COLUMN_LASTCLOCK
							, ItemData.COLUMN_UNITS
							, ItemData.TABLE_NAME+"."+ItemData.COLUMN_ITEMID+" AS "+ItemData.COLUMN_ITEMID
							, ItemData.TABLE_NAME+"."+ItemData.COLUMN_DESCRIPTION+" AS "+ItemData.COLUMN_DESCRIPTION
					}
					, null, null, null, null, null);
		}
		case URL_EVENT_BY_TRIGGER_ID: {
			String triggerid = uri.getPathSegments().get(1);

			try {
				zabbix.importEventByTriggerId(triggerid);
				zabbix.importTrigger(triggerid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(
					EventData.TABLE_NAME+" inner join "+TriggerData.TABLE_NAME+" on "+TriggerData.COLUMN_TRIGGERID+"="+EventData.COLUMN_OBJECTID
			);
			sqlBuilder.appendWhere(TriggerData.TABLE_NAME+"."+TriggerData.COLUMN_TRIGGERID+"="+triggerid);
			return sqlBuilder.query(
					zabbixLocalDB
					, new String[] {
							EventData.TABLE_NAME+"."+EventData.COLUMN_HOSTS+" AS hosts"
							, EventData.TABLE_NAME+"."+EventData.COLUMN_EVENTID+" AS eventid"
							, TriggerData.COLUMN_DESCRIPTION
							, TriggerData.COLUMN_PRIORITY
							, TriggerData.COLUMN_EXPRESSION
							, TriggerData.COLUMN_STATUS
							, TriggerData.COLUMN_TRIGGERID
							, EventData.COLUMN_CLOCK
							, EventData.COLUMN_ACK
							, EventData.TABLE_NAME+"."+EventData.COLUMN_VALUE
					}
					, null, null, null, null, null);
		}
		case URL_ITEM_BY_ID: {
			String itemid = uri.getPathSegments().get(1);

			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(ItemData.TABLE_NAME);
			sqlBuilder.appendWhere(ItemData.COLUMN_ITEMID+"="+itemid);
			return sqlBuilder.query(zabbixLocalDB, null, null, null, null, null, null, null);
		}
		case URL_ALL_SCREENS: {
			try {
				zabbix.importScreens();
			} catch (Exception e) {
				handleServiceException(e);
			}
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(ScreenData.TABLE_NAME);
			return sqlBuilder.query(zabbixLocalDB, null, null, null, null, null, ScreenData.COLUMN_NAME, null);
		}
		case URL_HISTORYDETAILS_BY_SCREEN__ID: {
			String screen__id = uri.getPathSegments().get(1);
			// find out zabbix screen__id
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(ScreenData.TABLE_NAME);
			sqlBuilder.appendWhere(ScreenData.COLUMN__ID+"="+screen__id);
			Cursor screenCursor = sqlBuilder.query(
				zabbixLocalDB,
				null,
				ScreenData.COLUMN__ID+"="+screen__id,
				null,
				null,
				null,
				null
			);
			screenCursor.moveToFirst();
			long screenid = screenCursor.getLong(screenCursor.getColumnIndex(ScreenData.COLUMN_SCREENID));
			screenCursor.close();

			try {
				zabbix.importHostsAndGroups();
				zabbix.importGraphsForScreen(screenid);
			} catch (Exception e) {
				handleServiceException(e);
			}

			// collect all itemids and then import the historydetails
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(GraphItemData.TABLE_NAME+" join "+GraphData.TABLE_NAME+" on "+GraphItemData.TABLE_NAME+"."+GraphItemData.COLUMN_GRAPHID+"="+GraphData.TABLE_NAME+"."+GraphData.COLUMN_GRAPHID+" join "+ScreenItemData.TABLE_NAME+" on "+ScreenItemData.COLUMN_RESOURCEID+"="+GraphData.TABLE_NAME+"."+GraphData.COLUMN_GRAPHID);
			sqlBuilder.appendWhere(ScreenItemData.TABLE_NAME+"."+ScreenItemData.COLUMN_SCREENID+"="+screenid);
			Set<Long> itemids = new HashSet<Long>();
			Cursor itemidsCursor = sqlBuilder.query(zabbixLocalDB, new String[] {GraphItemData.TABLE_NAME+"."+GraphItemData.COLUMN_ITEMID}, ScreenItemData.COLUMN_SCREENID+"="+screenid, null, null, null, null);
			while (itemidsCursor.moveToNext()) {
				itemids.add(itemidsCursor.getLong(itemidsCursor.getColumnIndex(GraphItemData.COLUMN_ITEMID)));
			}
			itemidsCursor.close();
			int itemidsSize = itemids.size();
			int i=0;
			for (Long itemid : itemids) {
				try {
					zabbix.transformProgress(i*100/itemidsSize, (i+1)*100/itemidsSize);
					zabbix.importHistoryDetails(itemid.toString());
				} catch (Exception e) {
					handleServiceException(e);
				}
				i++;
			}
			zabbix.transformProgress(0, 0);
			zabbix.showProgress(99);

			// huge join
			sqlBuilder = new SQLiteQueryBuilder();
			sqlBuilder.setTables(
					HistoryDetailData.TABLE_NAME+
					" join "+GraphItemData.TABLE_NAME+" on "+HistoryDetailData.TABLE_NAME+"."+HistoryDetailData.COLUMN_ITEMID+"="+GraphItemData.TABLE_NAME+"."+GraphItemData.COLUMN_ITEMID+
					" join "+GraphData.TABLE_NAME+" on "+GraphItemData.TABLE_NAME+"."+GraphItemData.COLUMN_GRAPHID+"="+GraphData.TABLE_NAME+"."+GraphData.COLUMN_GRAPHID+
					" join "+ScreenItemData.TABLE_NAME+" on "+ScreenItemData.COLUMN_RESOURCEID+"="+GraphData.TABLE_NAME+"."+GraphData.COLUMN_GRAPHID+
					" join "+ItemData.TABLE_NAME+" on "+ItemData.TABLE_NAME+"."+ItemData.COLUMN_ITEMID+"="+GraphItemData.TABLE_NAME+"."+GraphItemData.COLUMN_ITEMID+
					" join "+HostData.TABLE_NAME+" on "+HostData.TABLE_NAME+"."+HostData.COLUMN_HOSTID+"="+ItemData.TABLE_NAME+"."+ItemData.COLUMN_HOSTID
			);
			sqlBuilder.appendWhere(ScreenItemData.TABLE_NAME+"."+ScreenItemData.COLUMN_SCREENID+"="+screenid);
			return sqlBuilder.query(zabbixLocalDB, null, null, null, null, null, HistoryDetailData.TABLE_NAME+"."+HistoryDetailData.COLUMN_CLOCK+" ASC", null);
		}
		}

		throw new IllegalStateException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String arg2, String[] arg3) {
		if (uriMatcher.match(uri) == URL_EVENT_BY_ID) {
			String eventid = uri.getPathSegments().get(1);
			String comment = values.getAsString("_comment");

			boolean success=false;
			try {
				success = zabbix.acknowledgeEvent(eventid, comment);
			} catch (Exception e) {
				handleServiceException(e);
				return 0;
			}
			if (success) {
				values.remove("_comment");
				return zabbixLocalDB.update(EventData.TABLE_NAME, values, EventData.COLUMN_EVENTID+"="+eventid, null);
			}
		}
		return 0;
	}
}
