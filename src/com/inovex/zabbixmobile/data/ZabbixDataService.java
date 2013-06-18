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
import android.util.Log;
import android.view.LayoutInflater;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.MockDatabaseHelper;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.view.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.view.EventsListAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;

public class ZabbixDataService extends OrmLiteBaseService<MockDatabaseHelper> {

	private static final String TAG = ZabbixDataService.class.getSimpleName();
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	private HashMap<TriggerSeverity, EventsListAdapter> mEventsListAdapters;
	private HashMap<TriggerSeverity, EventsDetailsPagerAdapter> mEventsDetailsPagerAdapters;

	private Context mActivityContext;
	private LayoutInflater mInflater;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class ZabbixDataBinder extends Binder {
		public ZabbixDataService getService() {
			// Return this service instance so clients can call public methods
			return ZabbixDataService.this;
		}
	}

	public EventsListAdapter getEventsListAdapter(TriggerSeverity severity) {
		return mEventsListAdapters.get(severity);
	}

	public EventsDetailsPagerAdapter getEventsDetailsPagerAdapter(
			TriggerSeverity severity) {
		return mEventsDetailsPagerAdapters.get(severity);
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
		Log.d(TAG, "onCreate");
		
		// set up adapters
		mEventsListAdapters = new HashMap<TriggerSeverity, EventsListAdapter>(
				TriggerSeverity.values().length);
		mEventsDetailsPagerAdapters = new HashMap<TriggerSeverity, EventsDetailsPagerAdapter>(
				TriggerSeverity.values().length);

		for (TriggerSeverity s : TriggerSeverity.values()) {
			mEventsListAdapters.put(s, new EventsListAdapter(this,
					R.layout.events_list_item));
			mEventsDetailsPagerAdapters
					.put(s, new EventsDetailsPagerAdapter(s));
		}

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
	 * Loads all events with a given severity from the database asynchronously.
	 * After loading the events, the list and details adapters are updated.
	 * 
	 * @param severity
	 *            severity of the events to be retrieved
	 * @param adapter
	 *            list adapter to be updated with the results
	 * @param callback
	 *            callback to be notified of the changed list adapter
	 */
	public void loadEventsBySeverity(final TriggerSeverity severity) {

		new AsyncTask<Void, Void, Void>() {

			private List<Event> events;
			private EventsListAdapter adapter = mEventsListAdapters
					.get(severity);
			private EventsDetailsPagerAdapter detailsAdapter = mEventsDetailsPagerAdapters
					.get(severity);

			@Override
			protected Void doInBackground(Void... params) {
				events = new ArrayList<Event>();
				try {
					events = mDatabaseHelper.getEventsBySeverity(severity);
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
				if (adapter != null) {
					adapter.clear();
					adapter.addAll(events);
					adapter.notifyDataSetChanged();
				}

				if (detailsAdapter != null) {
					detailsAdapter.clear();
					detailsAdapter.addAll(events);
					detailsAdapter.notifyDataSetChanged();
				}
			}

		}.execute();

	}

	/**
	 * Sets the activity context, which is needed to inflate layout elements.
	 * 
	 * @param context
	 *            the context
	 */
	public void setActivityContext(Context context) {
		this.mActivityContext = context;
		this.mInflater = (LayoutInflater) mActivityContext
				.getSystemService(LAYOUT_INFLATER_SERVICE);
	}

	public LayoutInflater getInflater() {
		return mInflater;
	}

}
