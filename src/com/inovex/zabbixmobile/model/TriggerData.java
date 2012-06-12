package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class TriggerData extends BaseModelData {
	public final static String TABLE_NAME = "triggers";

	/** Trigger ID */
	public static final String COLUMN_TRIGGERID = "triggerid";
	/** Trigger name */
	public static final String COLUMN_DESCRIPTION = "description";
	/** Severity */
	public static final String COLUMN_PRIORITY = "priority";
	/** Status */
	public static final String COLUMN_STATUS = "status";
	/** Status */
	public static final String COLUMN_VALUE = "value";
	/** Time of last state change */
	public static final String COLUMN_LASTCHANGE = "lastchange";
	/** Description */
	public static final String COLUMN_COMMENTS = "comments";
	/** Expression */
	public static final String COLUMN_EXPRESSION = "expression";
	/** Expression */
	public static final String COLUMN_URL = "url";
	/** (only local) host id */
	public static final String COLUMN_HOSTID = "hostid";
	/** (only local) liste mit hosts als string */
	public static final String COLUMN_HOSTS = "hosts";
	/** (only local) itemid */
	public static final String COLUMN_ITEMID = "itemid";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_TRIGGERID + " LONG,"
				+ COLUMN_DESCRIPTION + " TEXT,"
				+ COLUMN_PRIORITY + " INTEGER,"
				+ COLUMN_STATUS + " INTEGER,"
				+ COLUMN_VALUE + " INTEGER,"
				+ COLUMN_LASTCHANGE + " INTEGER,"
				+ COLUMN_COMMENTS + " TEXT,"
				+ COLUMN_EXPRESSION + " TEXT,"
				+ COLUMN_URL + " TEXT,"
				+ COLUMN_HOSTS + " TEXT,"
				+ COLUMN_HOSTID + " LONG,"
				+ COLUMN_ITEMID + " LONG"
				+ ");"
		);
	}

	public static void drop(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}
}
