package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverities;
import com.inovex.zabbixmobile.view.EventsArrayAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.dao.Dao;

public class ZabbixDataService extends OrmLiteBaseService<MockDatabaseHelper> {

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
	 * Retrieves all events with the given severity from the database.
	 * 
	 * @param severity
	 * @return list of events with a matching severity
	 * @throws SQLException
	 */
	private List<Event> getEventsBySeverity(TriggerSeverities severity)
			throws SQLException {
		List<Event> events;
		events = mDatabaseHelper.getDao(Event.class).queryForAll();
		if (severity == TriggerSeverities.ALL)
			return events;
		// filter events by trigger severity
		List<Event> eventsBySeverity = new ArrayList<Event>();
		Trigger t;
		for (Event e : events) {
			t = e.getTrigger();
			if (t == null)
				break;
			if (t.getPriority() == severity.getNumber())
				eventsBySeverity.add(e);
		}
		events = eventsBySeverity;
		return events;
	}

	/**
	 * Loads all events with a given severity from the database asynchronously.
	 * After loading the events, the given list adapter is updated and the
	 * callback is notified of the changed adapter.
	 * 
	 * @param severity
	 *            severity of the events to be retrieved
	 * @param adapter
	 *            list adapter to be updated with the results
	 * @param callback
	 *            callback to be notified of the changed list adapter
	 */
	public void loadEventsBySeverity(final TriggerSeverities severity,
			final OnEventListLoadedListener callback) {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				List<Event> events = new ArrayList<Event>();
				try {
					events = getEventsBySeverity(severity);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				callback.onEventListLoaded(events);
				return null;
			}

		}.doInBackground();

	}

	/**
	 * Returns the ID of an event.
	 * 
	 * @param id
	 *            ID of the desired event
	 * @return the corresponding event
	 * @throws SQLException
	 */
	private Event getEventById(long id) throws SQLException {
		Dao<Event, Long> dao = mDatabaseHelper.getDao(Event.class);
		return dao.queryForId(id);
	}

	/**
	 * Asynchronously loads the event with the given ID from the database.
	 * 
	 * @param eventId
	 *            ID of the queried event
	 * @param callback
	 *            callback to be notified when the event has been loaded
	 * @return
	 */
	public Event loadEventById(final long eventId,
			final OnEventLoadedListener callback) {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Event e = getEventById(eventId);
					if (e == null)
						throw new RuntimeException("Event with ID " + eventId
								+ " could not be retrieved from the database");
					callback.onEventLoaded(e);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return null;
			}

		}.doInBackground();
		return null;
	}

}
