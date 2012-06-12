package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class CacheData extends BaseModelData {
	public final static String TABLE_NAME = "caches";

	/** timestamp unix */
	public static final String COLUMN_EXPIRE_DATE = "expire_date";
	/** table, action etc. */
	public static final String COLUMN_KIND = "kind";
	/** additional filter */
	public static final String COLUMN_FILTER = "filter";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_EXPIRE_DATE + " INTEGER,"
				+ COLUMN_KIND + " TEXT,"
				+ COLUMN_FILTER + " TEXT"
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
