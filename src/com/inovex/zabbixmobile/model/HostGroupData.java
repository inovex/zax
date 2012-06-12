package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class HostGroupData extends BaseModelData {
	public final static String TABLE_NAME = "hostgroups";

	/** Host ID */
	public static final String COLUMN_GROUPID = "groupid";
	/** Host name */
	public static final String COLUMN_NAME = "name";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_GROUPID + " LONG,"
				+ COLUMN_NAME + " TEXT"
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
