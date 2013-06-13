package com.inovex.zabbixmobile.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverities;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ZabbixDataService extends Service {

	private static final String TAG = ZabbixDataService.class.getSimpleName();
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class ZabbixDataBinder extends Binder {
		public ZabbixDataService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ZabbixDataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,
				"Binder " + this.toString() + ": intent " + intent.toString()
						+ " bound.");
		return mBinder;
	}

	@Override
	public void onCreate() {
		// set up SQLite connection using OrmLite
		mDatabaseHelper = OpenHelperManager.getHelper(this,
				MockDatabaseHelper.class);
		super.onCreate();
	}

	/**
	 * Sample test method returning a random number.
	 * 
	 * @return random number
	 */
	public int getRandomNumber() {
		Log.d(TAG, "ZabbixService:getRandomNumber() [" + this.toString() + "]");
		return new Random().nextInt(100);
	}

	/**
	 * Queries all events from the database.
	 * 
	 * @return list of all events
	 * @throws SQLException
	 */
	public List<Event> getAllEvents() throws SQLException {
		List<Event> events = mDatabaseHelper.getDao(Event.class).queryForAll();
		return events;
	}

	/**
	 * Returns a list of events for a given trigger severity.
	 * 
	 * @param severity
	 *            the severity
	 * @return list of events with the given severity
	 * @throws SQLException
	 */
	public List<Event> getEventsBySeverity(TriggerSeverities severity)
			throws SQLException {
		if (severity == TriggerSeverities.ALL)
			return getAllEvents();
		// TODO: replace this with a JOIN
		List<Event> events;
		events = mDatabaseHelper.getDao(Event.class).queryForAll();
		List<Event> eventsBySeverity = new ArrayList<Event>();
		Trigger t;
		for (Event e : events) {
			t = e.getTrigger();
			if (t == null)
				break;
			if (t.getPriority() == severity.getNumber())
				eventsBySeverity.add(e);
		}
		return eventsBySeverity;
	}

	/**
	 * Returns the ID of an event.
	 * 
	 * @param id
	 *            ID of the desired event
	 * @return the corresponding event
	 * @throws SQLException
	 */
	public Event getEventById(long id) throws SQLException {
		Dao<Event, Long> dao = mDatabaseHelper.getDao(Event.class);
		return dao.queryForId(id);
	}
}
