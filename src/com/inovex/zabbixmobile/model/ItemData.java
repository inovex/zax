package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class ItemData extends BaseModelData {
	public final static String TABLE_NAME = "items";

	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	/** Host ID */
	public static final String COLUMN_HOSTID = "hostid";
	/** Item description */
	public static final String COLUMN_DESCRIPTION = "description";
	/** Last check */
	public static final String COLUMN_LASTCLOCK = "lastclock";
	/** Last value */
	public static final String COLUMN_LASTVALUE = "lastvalue";
	/** Value units */
	public static final String COLUMN_UNITS = "units";
	/** Value type */
	public static final String COLUMN_VALUE_TYPE = "value_type";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_ITEMID + " LONG,"
				+ COLUMN_HOSTID + " LONG,"
				+ COLUMN_DESCRIPTION + " TEXT,"
				+ COLUMN_LASTCLOCK + " INTEGER,"
				+ COLUMN_LASTVALUE + " TEXT,"
				+ COLUMN_UNITS + " TEXT,"
				+ COLUMN_VALUE_TYPE + " INTEGER"
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
