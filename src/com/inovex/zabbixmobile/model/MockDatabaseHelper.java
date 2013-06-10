package com.inovex.zabbixmobile.model;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.inovex.zabbixmobile.R;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

public class MockDatabaseHelper extends DatabaseHelper {

	// name of the database file for your application -- change to something
	// appropriate for your app
	private static final String DATABASE_NAME = "zabbixmobile.db";
	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;

	public MockDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION,
				R.raw.ormlite_config);

	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		super.onCreate(db, connectionSource);
		try {
			Dao<Event, Integer> eventDao = getDao(Event.class);
			Dao<Trigger, Integer> triggerDao = getDao(Trigger.class);
			Dao<Item, Integer> itemDao = getDao(Item.class);
			Dao<Host, Integer> hostDao = getDao(Host.class);
			eventDao.create(new Event(20616, 0, 1370861291, 1, false, false));
			eventDao.create(new Event(20617, 0, 1370861391, 0, false, false));
			triggerDao.create(new Trigger(14062, "This is a sample trigger.",
					"{13513}>0", "Comments...", 1370861291, 2, 0, 1, "URL",
					false));
			triggerDao.create(new Trigger(14063,
					"This also is a sample trigger.", "{13518}>0",
					"Comments...", 1370861291, 4, 0, 1, "URL", false));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
