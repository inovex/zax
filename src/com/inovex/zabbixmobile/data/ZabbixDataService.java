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
import android.widget.SeekBar;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.BaseSeverityFilterActivity;
import com.inovex.zabbixmobile.activities.ChecksActivity;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.ProblemsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListFragment;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.adapters.BaseSeverityPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsListAdapter;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.adapters.HostsListAdapter;
import com.inovex.zabbixmobile.adapters.ProblemsDetailsPagerAdapter;
import com.inovex.zabbixmobile.adapters.ProblemsListAdapter;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ZabbixDataService extends Service {

	public interface OnLoginProgressListener {

		public void onLoginStarted();

		public void onLoginFinished(boolean success);

	}

	private static final String TAG = ZabbixDataService.class.getSimpleName();

	public static final String EXTRA_USE_MOCK_DATA = "use_mock_data";
	boolean mUseMockData = false;
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	private HostGroupsSpinnerAdapter mHostGroupSpinnerAdapter;

	/**
	 * Adapters maintained by {@link ZabbixDataService}.
	 */
	// Events
	private HashMap<TriggerSeverity, EventsListAdapter> mEventsListAdapters;
	private HashMap<TriggerSeverity, EventsDetailsPagerAdapter> mEventsDetailsPagerAdapters;

	// Problems
	private HashMap<TriggerSeverity, ProblemsListAdapter> mProblemsListAdapters;
	private HashMap<TriggerSeverity, ProblemsDetailsPagerAdapter> mProblemsDetailsPagerAdapters;

	// Checks
	private HostsListAdapter mHostsListAdapter;

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

	/**
	 * Returns an event list adapter.
	 * 
	 * See {@link EventsListPage}.
	 * 
	 * @param severity
	 *            severity of the adapter
	 * @return list adapter
	 */
	public BaseServiceAdapter<Event> getEventsListAdapter(
			TriggerSeverity severity) {
		return mEventsListAdapters.get(severity);
	}

	/**
	 * Returns an event details adapter to be used by the view pager.
	 * 
	 * See {@link EventsDetailsFragment}.
	 * 
	 * @param severity
	 *            severity of the adapter
	 * @return details pager adapter
	 */
	public BaseSeverityPagerAdapter<Event> getEventsDetailsPagerAdapter(
			TriggerSeverity severity) {
		return mEventsDetailsPagerAdapters.get(severity);
	}

	/**
	 * Returns the adapter for the host group spinner.
	 * 
	 * See {@link BaseSeverityFilterActivity}, {@link ChecksActivity}.
	 * 
	 * @return spinner adapter
	 */
	public HostGroupsSpinnerAdapter getHostGroupSpinnerAdapter() {
		return mHostGroupSpinnerAdapter;
	}

	/**
	 * Returns a problems list adapter.
	 * 
	 * See {@link ProblemsListPage}.
	 * 
	 * @param severity
	 *            severity of the adapter
	 * @return list adapter
	 */
	public BaseServiceAdapter<Trigger> getProblemsListAdapter(
			TriggerSeverity severity) {
		return mProblemsListAdapters.get(severity);
	}

	/**
	 * Returns a problems details adapter to be used by the view pager.
	 * 
	 * See {@link ProblemsDetailsFragment}.
	 * 
	 * @param severity
	 *            severity of the adapter
	 * @return details pager adapter
	 */
	public BaseSeverityPagerAdapter<Trigger> getProblemsDetailsPagerAdapter(
			TriggerSeverity severity) {
		return mProblemsDetailsPagerAdapters.get(severity);
	}

	/**
	 * Returns a host list adapter.
	 * 
	 * See {@link ChecksListFragment}.
	 * 
	 * @return host list adapter
	 */
	public HostsListAdapter getHostsListAdapter() {
		return mHostsListAdapter;
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

		mProblemsListAdapters = new HashMap<TriggerSeverity, ProblemsListAdapter>(
				TriggerSeverity.values().length);
		mProblemsDetailsPagerAdapters = new HashMap<TriggerSeverity, ProblemsDetailsPagerAdapter>(
				TriggerSeverity.values().length);

		mHostGroupSpinnerAdapter = new HostGroupsSpinnerAdapter(this);

		for (TriggerSeverity s : TriggerSeverity.values()) {
			mEventsListAdapters.put(s, new EventsListAdapter(this));
			mEventsDetailsPagerAdapters
					.put(s, new EventsDetailsPagerAdapter(s));
		}

		for (TriggerSeverity s : TriggerSeverity.values()) {
			mProblemsListAdapters.put(s, new ProblemsListAdapter(this));
			mProblemsDetailsPagerAdapters.put(s,
					new ProblemsDetailsPagerAdapter(s));
		}

		mHostsListAdapter = new HostsListAdapter(this);

	}

	/**
	 * Loads all events with a given severity and host group from the database
	 * asynchronously. After loading the events, the corresponding adapters are
	 * updated. If necessary, an import from the Zabbix API is triggered.
	 * 
	 * @param severity
	 *            severity of the events to be retrieved
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapters will be cleared before being filled with entries
	 *            matching the selected host group.
	 */
	public void loadEventsBySeverityAndHostGroup(
			final TriggerSeverity severity, final long hostGroupId,
			final boolean hostGroupChanged) {

		new RemoteAPITask(mRemoteAPI) {

			private List<Event> events;
			private BaseServiceAdapter<Event> adapter = mEventsListAdapters
					.get(severity);
			private BaseSeverityPagerAdapter<Event> detailsAdapter = mEventsDetailsPagerAdapters
					.get(severity);

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				events = new ArrayList<Event>();
				try {
					mRemoteAPI.importEvents();
				} finally {
					// even if the api call is not successful, we can still use
					// the cached events
					try {
						events = mDatabaseHelper
								.getEventsBySeverityAndHostGroupId(severity,
										hostGroupId);
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
					if (hostGroupChanged)
						adapter.clear();
					adapter.addAll(events);
					adapter.notifyDataSetChanged();
				}

				if (detailsAdapter != null) {
					if (hostGroupChanged)
						detailsAdapter.clear();
					detailsAdapter.addAll(events);
					detailsAdapter.notifyDataSetChanged();
				}
			}

		}.execute();

	}

	/**
	 * Loads all triggers with a given severity and host group from the database
	 * asynchronously. After loading the events, the corresponding adapters are
	 * updated. If necessary, an import from the Zabbix API is triggered.
	 * 
	 * @param severity
	 *            severity of the events to be retrieved
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapters will be cleared before being filled with entries
	 *            matching the selected host group.
	 */
	public void loadTriggersBySeverityAndHostGroup(
			final TriggerSeverity severity, final long hostGroupId,
			final boolean hostGroupChanged) {

		new RemoteAPITask(mRemoteAPI) {

			private List<Trigger> triggers;
			private BaseServiceAdapter<Trigger> adapter = mProblemsListAdapters
					.get(severity);
			private BaseSeverityPagerAdapter<Trigger> detailsAdapter = mProblemsDetailsPagerAdapters
					.get(severity);

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				triggers = new ArrayList<Trigger>();
				try {
					mRemoteAPI.importActiveTriggers();
					// even if the api call is not successful, we can still use
					// the cached events
				} finally {
					try {
						triggers = mDatabaseHelper
								.getTriggersBySeverityAndHostGroupId(severity,
										hostGroupId);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (adapter != null) {
					if (hostGroupChanged)
						adapter.clear();
					adapter.addAll(triggers);
					adapter.notifyDataSetChanged();
				}

				if (detailsAdapter != null) {
					if (hostGroupChanged)
						detailsAdapter.clear();
					detailsAdapter.addAll(triggers);
					detailsAdapter.notifyDataSetChanged();
				}
			}

		}.execute();

	}

	/**
	 * Loads host groups from the database (if necessary, a Zabbix API call is
	 * triggered) and updates the host group spinner adapter.
	 */
	public void loadHostGroups() {
		new RemoteAPITask(mRemoteAPI) {

			private List<HostGroup> hostGroups;
			private BaseServiceAdapter<HostGroup> groupsAdapter = mHostGroupSpinnerAdapter;

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
				if (groupsAdapter != null) {
					// adapter.clear();
					groupsAdapter.addAll(hostGroups);
					groupsAdapter.notifyDataSetChanged();
				}

			}

		}.execute();
	}

	/**
	 * Loads all hosts with a given host group from the database asynchronously.
	 * After loading the events, the host list adapter is updated. If necessary,
	 * an import from the Zabbix API is triggered.
	 * 
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapter will be cleared before being filled with entries
	 *            matching the selected host group.
	 */
	public void loadHostsByHostGroup(final long hostGroupId,
			final boolean hostGroupChanged) {
		new RemoteAPITask(mRemoteAPI) {

			private List<Host> hosts;
			private BaseServiceAdapter<Host> hostsAdapter = mHostsListAdapter;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				hosts = new ArrayList<Host>();
				try {
					mRemoteAPI.importHostsAndGroups();
					// even if the api call is not successful, we can still use
					// the cached events
				} finally {
					try {
						hosts = mDatabaseHelper
								.getHostsByHostGroup(hostGroupId);
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
				if (hostsAdapter != null) {
					if (hostGroupChanged)
						hostsAdapter.clear();
					hostsAdapter.addAll(hosts);
					hostsAdapter.notifyDataSetChanged();
				}

			}

		}.execute();
	}

	/**
	 * Loads all applications with a given host group from the database
	 * asynchronously. After loading the events, the corresponding adapters are
	 * updated. If necessary, an import from the Zabbix API is triggered.
	 * 
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapters will be cleared before being filled with entries
	 *            matching the selected host group.
	 */
	public void loadApplications() {
		new RemoteAPITask(mRemoteAPI) {

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				mRemoteAPI.importApplications();
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
			}

		}.execute();
	}

	/**
	 * Sets the activity context, which is needed to inflate layout elements.
	 * This also initializes the layout inflater
	 * {@link ZabbixDataService#mInflater}.
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
