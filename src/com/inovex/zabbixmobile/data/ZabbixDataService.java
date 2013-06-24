package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.view.BaseServiceAdapter;
import com.inovex.zabbixmobile.view.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.view.EventsListAdapter;
import com.inovex.zabbixmobile.view.HostGroupsSpinnerAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ZabbixDataService extends Service {

	public interface OnLoginProgressListener {

		public void onLoginStarted();

		public void onLoginFinished(boolean success);

	}

	private static final String TAG = ZabbixDataService.class.getSimpleName();

	public static final String EXTRA_USE_MOCK_DATA = "use_mock_data";
	boolean mUseMockData = false;
	public static final int MESSAGE_LOGIN_STARTED = 1;
	public static final int MESSAGE_LOGIN_FINISHED = 2;
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	private HashMap<TriggerSeverity, EventsListAdapter> mEventsListAdapters;
	private HashMap<TriggerSeverity, EventsDetailsPagerAdapter> mEventsDetailsPagerAdapters;
	
	private HostGroupsSpinnerAdapter mHostGroupSpinnerAdapter;

	private Context mActivityContext;
	private LayoutInflater mInflater;
	private ZabbixRemoteAPI mRemoteAPI;

	protected boolean loggedIn;

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

	public BaseServiceAdapter<Event> getEventsListAdapter(TriggerSeverity severity) {
		return mEventsListAdapters.get(severity);
	}

	public EventsDetailsPagerAdapter getEventsDetailsPagerAdapter(
			TriggerSeverity severity) {
		return mEventsDetailsPagerAdapters.get(severity);
	}
	
	public HostGroupsSpinnerAdapter getHostGroupSpinnerAdapter() {
		return mHostGroupSpinnerAdapter;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,
				"Binder " + this.toString() + ": intent " + intent.toString()
						+ " bound.");

		if (intent.hasExtra(EXTRA_USE_MOCK_DATA)
				&& intent.getBooleanExtra(EXTRA_USE_MOCK_DATA, false)) {
			mUseMockData = true;
		}

		if (mDatabaseHelper == null) {
			// set up SQLite connection using OrmLite
			if (mUseMockData)
				mDatabaseHelper = OpenHelperManager.getHelper(this,
						MockDatabaseHelper.class);
			else
				mDatabaseHelper = OpenHelperManager.getHelper(this,
						DatabaseHelper.class);
			// recreate database
			mDatabaseHelper.onUpgrade(mDatabaseHelper.getWritableDatabase(), 0,
					1);
			Log.d(TAG, "onCreate");
		}
		if (mRemoteAPI == null) {
			mRemoteAPI = new ZabbixRemoteAPI(this.getApplicationContext(),
					mDatabaseHelper);
		}

		return mBinder;
	}

	/**
	 * Performs the Zabbix login using the server address and credentials from
	 * the preferences.
	 * 
	 * @param listener
	 *            listener to be informed about start and end of the login
	 *            process
	 * 
	 */
	public void performZabbixLogin(final OnLoginProgressListener listener) {
		if (!loggedIn) {
			listener.onLoginStarted();

			// authenticate
			RemoteAPITask loginTask = new RemoteAPITask(mRemoteAPI) {

				@Override
				protected void executeTask()
						throws ZabbixLoginRequiredException, FatalException {
					mRemoteAPI.authenticate();
					loggedIn = true;
				}

				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					listener.onLoginFinished(loggedIn);
				}

			};
			loginTask.execute();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// set up adapters
		mEventsListAdapters = new HashMap<TriggerSeverity, EventsListAdapter>(
				TriggerSeverity.values().length);
		mEventsDetailsPagerAdapters = new HashMap<TriggerSeverity, EventsDetailsPagerAdapter>(
				TriggerSeverity.values().length);

		for (TriggerSeverity s : TriggerSeverity.values()) {
			mEventsListAdapters.put(s, new EventsListAdapter(this));
			mEventsDetailsPagerAdapters
					.put(s, new EventsDetailsPagerAdapter(s));
		}
		
		mHostGroupSpinnerAdapter = new HostGroupsSpinnerAdapter(this);

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
	public void loadEventsBySeverityAndHostGroup(final TriggerSeverity severity, final long hostGroupId) {

		new RemoteAPITask(mRemoteAPI) {

			private List<Event> events;
			private BaseServiceAdapter<Event> adapter = mEventsListAdapters
					.get(severity);
			private EventsDetailsPagerAdapter detailsAdapter = mEventsDetailsPagerAdapters
					.get(severity);

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				events = new ArrayList<Event>();
				try {
					mRemoteAPI.importEvents();
					// even if the api call is not successful, we can still use
					// the cached events
				} finally {
					try {
						events = mDatabaseHelper.getEventsBySeverityAndHostGroupId(severity, hostGroupId);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
	
	public void loadHostGroups() {
		new RemoteAPITask(mRemoteAPI) {

			private List<HostGroup> hostGroups;
			private BaseServiceAdapter<HostGroup> adapter = mHostGroupSpinnerAdapter;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				hostGroups = new ArrayList<HostGroup>();
				try {
					mRemoteAPI.importHostsAndGroups();
					// even if the api call is not successful, we can still use
					// the cached events
				} finally {
					try {
						hostGroups = mDatabaseHelper.getHostGroups();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// TODO: update the data set instead of removing and re-adding
				// all items
				if (adapter != null) {
					adapter.clear();
					adapter.addAll(hostGroups);
					adapter.notifyDataSetChanged();
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
