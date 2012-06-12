package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class HostData extends BaseModelData {
	public final static String TABLE_NAME = "hosts";

	/** Host ID */
	public static final String COLUMN_HOSTID = "hostid";
	/** Host name */
	public static final String COLUMN_HOST = "host";
	/** (only local) Host Group Id */
	public static final String COLUMN_GROUPID = "groupid";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_HOSTID + " LONG,"
				+ COLUMN_GROUPID + " LONG,"
				+ COLUMN_HOST + " TEXT"
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
