package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class GraphData extends BaseModelData {
	public final static String TABLE_NAME = "graphs";

	/** Graph ID */
	public static final String COLUMN_GRAPHID = "graphid";
	/** Graph name */
	public static final String COLUMN_NAME = "name";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_GRAPHID + " LONG,"
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
