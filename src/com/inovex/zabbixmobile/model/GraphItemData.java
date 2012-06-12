package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class GraphItemData extends BaseModelData {
	public final static String TABLE_NAME = "graphitems";

	/** GraphItem ID- original gitemid */
	public static final String COLUMN_GRAPHITEMID = "graphitemid";
	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	/** graph id */
	public static final String COLUMN_GRAPHID = "graphid";
	/** color hex (only local, original as string) */
	public static final String COLUMN_COLOR = "color";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_GRAPHITEMID + " LONG,"
				+ COLUMN_ITEMID + " LONG,"
				+ COLUMN_GRAPHID + " LONG,"
				+ COLUMN_COLOR + " INTEGER"
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
