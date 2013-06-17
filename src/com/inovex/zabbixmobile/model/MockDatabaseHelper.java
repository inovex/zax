package com.inovex.zabbixmobile.model;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
	private static final String TAG = MockDatabaseHelper.class.getSimpleName();

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
			Event[] events = new Event[] {
					new Event(12345, 0, System.currentTimeMillis()
							- (3600 * 1000 * 12), 1, false, false),
					new Event(13467, 0, System.currentTimeMillis()
							- (3600 * 1000 * 10), 0, true, false),
					new Event(17231, 0, System.currentTimeMillis()
							- (3600 * 1000 * 7), 1, false, true),
					new Event(19865, 0, System.currentTimeMillis()
							- (3600 * 1000 * 5), 0, false, false),
					new Event(14562, 0, System.currentTimeMillis()
							- (3600 * 1000 * 9), 1, true, true),
					new Event(19872, 0, System.currentTimeMillis()
							- (3600 * 1000 * 4), 0, false, false),
					new Event(20616, 0, System.currentTimeMillis()
							- (3600 * 1000 * 3), 1, true, false),
					new Event(21576, 0, System.currentTimeMillis()
							- (3600 * 1000 * 2), 0, false, true),
					new Event(25821, 0, System.currentTimeMillis()
							- (3600 * 1000 * 0), 1, true, true),
					new Event(14529, 0, System.currentTimeMillis()
							- (3600 * 1000 * 8), 0, false, false) };
			for (Event e : events) {
				eventDao.create(e);
			}
			Trigger[] triggers = new Trigger[] {
					new Trigger(14062, "Sample trigger #1", "{13513}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 12), TriggerSeverity.AVERAGE, 0, 1, "URL", false),
					new Trigger(14063, "Sample trigger #2", "{1}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 10), TriggerSeverity.DISASTER, 0, 1, "URL", false),
					new Trigger(14064, "Sample trigger #3", "{32415}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 7), TriggerSeverity.HIGH, 0, 1, "URL", false),
					new Trigger(14065, "Sample trigger #4", "{13518}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 5), TriggerSeverity.NOT_CLASSIFIED, 1, 1, "URL", false),
					new Trigger(14066, "Sample trigger #5", "{12}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 9), TriggerSeverity.WARNING, 0, 1, "URL", false),
					new Trigger(14067, "Sample trigger #6", "{13518}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 4), TriggerSeverity.AVERAGE, 1, 1, "URL", false),
					new Trigger(14068, "Sample trigger #7", "{431}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 3), TriggerSeverity.HIGH, 1, 1, "URL", false),
					new Trigger(14069, "Sample trigger #8", "{13518}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 2), TriggerSeverity.INFORMATION, 0, 1, "URL", false),
					new Trigger(14070, "Sample trigger #9", "{123}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 0), TriggerSeverity.HIGH, 1, 1, "URL", false),
					new Trigger(14071, "Sample trigger #10", "{13518}>0",
							"Comments...", System.currentTimeMillis()
									- (3600 * 1000 * 8), TriggerSeverity.INFORMATION, 0, 1, "URL", false)

			};
			int i = 0;
			for (Trigger t : triggers) {
				triggerDao.create(t);
				events[i].setTrigger(t);
				eventDao.update(events[i]);
				i++;
			}
		} catch (SQLException e) {
			Log.e(TAG, e.toString());
		}
	}

}
