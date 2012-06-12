package com.inovex.zabbixmobile.model;

import android.database.sqlite.SQLiteDatabase;

public class EventData extends BaseModelData {
	public final static String TABLE_NAME = "events";

	/** Event ID*/
	public static final String COLUMN_EVENTID = "eventid";
	/** Related object ID*/
	public static final String COLUMN_OBJECTID = "objectid";
	/** Time of generated event */
	public static final String COLUMN_CLOCK = "clock";
	/** Status */
	public static final String COLUMN_VALUE = "value";
	/** Flag indicating event ack */
	public static final String COLUMN_ACK = "acknowledged";
	/** (only local) Names of the hosts, comma-separated */
	public static final String COLUMN_HOSTS = "host";

	public static void create(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE "+TABLE_NAME+" ("
				+ COLUMN__ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_EVENTID + " LONG,"
				+ COLUMN_OBJECTID + " LONG,"
				+ COLUMN_CLOCK + " INTEGER,"
				+ COLUMN_VALUE + " INTEGER,"
				+ COLUMN_ACK + " BOOLEAN,"
				+ COLUMN_HOSTS + " TEXT"
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
