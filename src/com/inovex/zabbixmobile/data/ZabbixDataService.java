package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverities;
import com.inovex.zabbixmobile.view.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.view.EventsListAdapter;
import com.inovex.zabbixmobile.view.IEventsListAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.dao.Dao;

public class ZabbixDataService extends OrmLiteBaseService<MockDatabaseHelper> {

	private static final String TAG = ZabbixDataService.class.getSimpleName();
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	private HashMap<TriggerSeverities, EventsListAdapter> mEventsListAdapters;
	private HashMap<TriggerSeverities, EventsDetailsPagerAdapter> mEventsDetailsPagerAdapters;

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

	public EventsListAdapter getEventsListAdapter(TriggerSeverities severity) {
		return mEventsListAdapters.get(severity);
	}

	public EventsDetailsPagerAdapter getEventsDetailsPagerAdapter(
			TriggerSeverities severity, FragmentManager fm, Context context) {
		EventsDetailsPagerAdapter adapter = mEventsDetailsPagerAdapters
				.get(severity);
		if (adapter == null) {
			adapter = new EventsDetailsPagerAdapter(context, fm, severity);
			mEventsDetailsPagerAdapters.put(severity, adapter);
		}
		return adapter;
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
		super.onCreate();
		// set up SQLite connection using OrmLite
		mDatabaseHelper = OpenHelperManager.getHelper(this,
				MockDatabaseHelper.class);
		// mDatabaseHelper.onUpgrade(mDatabaseHelper.getWritableDatabase(), 0,
		// 1);
		Log.d(TAG, "onCreate");

		// set up adapters
		mEventsListAdapters = new HashMap<TriggerSeverities, EventsListAdapter>(
				TriggerSeverities.values().length);
		for(TriggerSeverities s : TriggerSeverities.values()) {
			mEventsListAdapters.put(s, new EventsListAdapter(getApplicationContext(), R.layout.events_list_item));
		}
		mEventsDetailsPagerAdapters = new HashMap<TriggerSeverities, EventsDetailsPagerAdapter>(
				TriggerSeverities.values().length);

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
			if (t.getPriority() == severity)
				eventsBySeverity.add(e);
		}
		events = eventsBySeverity;
		return events;
	}

	/**
	 * Loads all events with a given severity without providing a callback. See
	 * {@link ZabbixDataService#loadEventsBySeverity(TriggerSeverities, IEventsListAdapter, OnDataAccessFinishedListener)}
	 * .
	 * 
	 * @param severity
	 *            severity of the events to be retrieved
	 * @param adapter
	 *            list adapter to be updated with the results
	 */
	public void loadEventsBySeverity(final TriggerSeverities severity,
			final IEventsListAdapter adapter) {
		loadEventsBySeverity(severity, adapter, null);
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
			final IEventsListAdapter adapter,
			final OnDataAccessFinishedListener callback) {

		new AsyncTask<Void, Void, Void>() {

			private List<Event> events;

			@Override
			protected Void doInBackground(Void... params) {
				events = new ArrayList<Event>();
				try {
					events = getEventsBySeverity(severity);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				adapter.addAll(events);
				adapter.notifyDataSetChanged();
				if (callback != null)
					callback.onDataAccessFinished();
			}

		}.execute();

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
	public void loadEventsBySeverity(final TriggerSeverities severity) {

		new AsyncTask<Void, Void, Void>() {

			private List<Event> events;
			private EventsListAdapter adapter = mEventsListAdapters
					.get(severity);

			@Override
			protected Void doInBackground(Void... params) {
				events = new ArrayList<Event>();
				try {
					events = getEventsBySeverity(severity);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// TODO: update the data set instead of removing and re-adding
				// all items
				adapter.clear();
				adapter.addAll(events);
				adapter.notifyDataSetChanged();
			}

		}.execute();

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

}
