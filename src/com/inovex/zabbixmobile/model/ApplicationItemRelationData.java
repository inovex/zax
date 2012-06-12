package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class ApplicationItemRelationData extends BaseModelData {
	public final static String TABLE_NAME = "applicationitemrelations";

	/** application id (zabbix) */
	public static final String COLUMN_APPLICATIONID = "applicationid";
	/** item id (zabbix) */
	public static final String COLUMN_ITEMID = "itemid";
	public static final String COLUMN_HOSTID = "hostid";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_APPLICATIONID + " LONG,"
				+ COLUMN_ITEMID + " LONG,"
				+ COLUMN_HOSTID + " LONG"
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
