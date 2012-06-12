package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class ScreenItemData extends BaseModelData {
	public final static String TABLE_NAME = "screenitems";

	/** screen Item ID */
	public static final String COLUMN_SCREENITEMID = "screenitemid";
	/** screen ID */
	public static final String COLUMN_SCREENID = "screenid";
	/** graph id */
	public static final String COLUMN_RESOURCEID = "resourceid";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_SCREENITEMID + " LONG,"
				+ COLUMN_SCREENID + " LONG,"
				+ COLUMN_RESOURCEID + " LONG"
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
