package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class HistoryDetailData extends BaseModelData {
	public final static String TABLE_NAME = "historydetails";

	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	/** Unix timestamp */
	public static final String COLUMN_CLOCK = "clock";
	/** Item value */
	public static final String COLUMN_VALUE = "value";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_ITEMID + " LONG,"
				+ COLUMN_VALUE + " REAL,"
				+ COLUMN_CLOCK + " INTEGER"
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
