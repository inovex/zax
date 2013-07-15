package com.inovex.zabbixmobile.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;

import com.inovex.zabbixmobile.activities.BaseSeverityFilterActivity;
import com.inovex.zabbixmobile.activities.ChecksActivity;
import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.adapters.BaseSeverityPagerAdapter;
import com.inovex.zabbixmobile.adapters.ChecksApplicationsPagerAdapter;
import com.inovex.zabbixmobile.adapters.ChecksItemsListAdapter;
import com.inovex.zabbixmobile.adapters.ChecksItemsPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsListAdapter;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.adapters.HostsListAdapter;
import com.inovex.zabbixmobile.adapters.ProblemsDetailsPagerAdapter;
import com.inovex.zabbixmobile.adapters.ProblemsListAdapter;
import com.inovex.zabbixmobile.exceptions.FatalException;
import com.inovex.zabbixmobile.exceptions.ZabbixLoginRequiredException;
import com.inovex.zabbixmobile.listeners.OnAcknowledgeEventListener;
import com.inovex.zabbixmobile.listeners.OnHistoryDetailsLoadedListener;
import com.inovex.zabbixmobile.listeners.OnListAdapterLoadedListener;
import com.inovex.zabbixmobile.listeners.OnSeverityListAdapterLoadedListener;
import com.inovex.zabbixmobile.model.Application;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Item;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ZabbixDataService extends Service {

	public interface OnLoginProgressListener {

		public void onLoginStarted();

		public void onLoginFinished(boolean success);

	}

	private static final String TAG = ZabbixDataService.class.getSimpleName();

	public static final String EXTRA_IS_TESTING = "is_testing";
	boolean mIsTesting = false;
	// Binder given to clients
	private final IBinder mBinder = new ZabbixDataBinder();

	private DatabaseHelper mDatabaseHelper;

	private HostGroupsSpinnerAdapter mHostGroupsSpinnerAdapter;

	/**
	 * Adapters maintained by {@link ZabbixDataService}.
	 */
	// Events
	private HashMap<TriggerSeverity, EventsListAdapter> mEventsListAdapters;
	private HashMap<TriggerSeverity, EventsDetailsPagerAdapter> mEventsDetailsPagerAdapters;

	// Problems
	private HashMap<TriggerSeverity, ProblemsListAdapter> mProblemsListAdapters;
	private ProblemsListAdapter mProblemsMainListAdapter;
	private HashMap<TriggerSeverity, ProblemsDetailsPagerAdapter> mProblemsDetailsPagerAdapters;

	// Checks
	private HostsListAdapter mHostsListAdapter;
	private ChecksApplicationsPagerAdapter mChecksApplicationsPagerAdapter;
	private ChecksItemsListAdapter mChecksItemsListAdapter;
	private ChecksItemsPagerAdapter mChecksItemsPagerAdapter;

	private Context mActivityContext;
	private LayoutInflater mInflater;
	private ZabbixRemoteAPI mRemoteAPI;

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
		return mHostGroupsSpinnerAdapter;
	}

	/**
	 * Returns the problems list adapter for the main view. This adapter
	 * contains all active problems regardless of severity and hostgroup.
	 * 
	 * @return list adapter
	 */
	public BaseServiceAdapter<Trigger> getProblemsMainListAdapter() {
		return mProblemsMainListAdapter;
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

	/**
	 * Returns the application adapter.
	 * 
	 * @return
	 */
	public ChecksApplicationsPagerAdapter getChecksApplicationsPagerAdapter() {
		return mChecksApplicationsPagerAdapter;
	}

	/**
	 * Returns the application items list adapter.
	 * 
	 * @return
	 */
	public ChecksItemsListAdapter getChecksItemsListAdapter() {
		return mChecksItemsListAdapter;
	}

	/**
	 * Returns the application items pager adapter.
	 * 
	 * @return
	 */
	public ChecksItemsPagerAdapter getChecksItemsPagerAdapter() {
		return mChecksItemsPagerAdapter;
	}

	/**
	 * Retrieves the host with the given ID from the database.
	 * 
	 * @param hostId
	 * @return
	 */
	public Host getHostById(long hostId) {
		try {
			return mDatabaseHelper.getHostById(hostId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves the event with the given ID from the database.
	 * 
	 * @param eventId
	 * @return
	 */
	public Event getEventById(long eventId) {
		try {
			return mDatabaseHelper.getEventById(eventId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves the trigger with the given ID from the database.
	 * 
	 * @param triggerId
	 * @return
	 */
	public Trigger getTriggerById(long triggerId) {
		try {
			return mDatabaseHelper.getTriggerById(triggerId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private int bindings = 0;

	@Override
	public IBinder onBind(Intent intent) {
		bindings++;
		Log.d(TAG, "onBind: " + bindings);
		Log.d(TAG,
				"Binder " + this.toString() + ": intent " + intent.toString()
						+ " bound.");

		if (intent.getBooleanExtra(EXTRA_IS_TESTING, false)) {
			mIsTesting = true;
		}

		if (!mIsTesting) {
			onBind(null, null);
		}

		return mBinder;
	}

	public void onBind(DatabaseHelper databasehelperMock,
			ZabbixRemoteAPI remoteAPIMock) {
		if (mDatabaseHelper == null) {
			// set up SQLite connection using OrmLite
			if (databasehelperMock != null) {
				mDatabaseHelper = databasehelperMock;
			} else {
				mDatabaseHelper = OpenHelperManager.getHelper(this,
						DatabaseHelper.class);
				// recreate database
				mDatabaseHelper.onUpgrade(
						mDatabaseHelper.getWritableDatabase(), 0, 1);
			}

			Log.d(TAG, "onCreate");
		}
		if (mRemoteAPI == null) {
			if (remoteAPIMock != null) {
				mRemoteAPI = remoteAPIMock;
			} else {
				mRemoteAPI = new ZabbixRemoteAPI(this.getApplicationContext(),
						mDatabaseHelper, null, null);
			}
		}
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
		listener.onLoginStarted();

		// authenticate
		RemoteAPITask loginTask = new RemoteAPITask(mRemoteAPI) {

			private boolean success = false;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				mRemoteAPI.authenticate();
				success = true;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				listener.onLoginFinished(success);
			}

		};
		loginTask.execute();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

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

		mProblemsListAdapters = new HashMap<TriggerSeverity, ProblemsListAdapter>(
				TriggerSeverity.values().length);
		mProblemsMainListAdapter = new ProblemsListAdapter(this);
		mProblemsDetailsPagerAdapters = new HashMap<TriggerSeverity, ProblemsDetailsPagerAdapter>(
				TriggerSeverity.values().length);

		for (TriggerSeverity s : TriggerSeverity.values()) {
			mProblemsListAdapters.put(s, new ProblemsListAdapter(this));
			mProblemsDetailsPagerAdapters.put(s,
					new ProblemsDetailsPagerAdapter(s));
		}

		mHostGroupsSpinnerAdapter = new HostGroupsSpinnerAdapter(this);

		mHostsListAdapter = new HostsListAdapter(this);
		mChecksApplicationsPagerAdapter = new ChecksApplicationsPagerAdapter();
		mChecksItemsListAdapter = new ChecksItemsListAdapter(this);
		mChecksItemsPagerAdapter = new ChecksItemsPagerAdapter();

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
	 * @param callback
	 *            listener to be called when the adapters have been updated
	 */
	public void loadEventsBySeverityAndHostGroup(
			final TriggerSeverity severity, final long hostGroupId,
			final boolean hostGroupChanged,
			final OnSeverityListAdapterLoadedListener callback) {

		new RemoteAPITask(mRemoteAPI) {

			private List<Event> events;
			private final BaseServiceAdapter<Event> listAdapter = mEventsListAdapters
					.get(severity);
			private final BaseSeverityPagerAdapter<Event> detailsAdapter = mEventsDetailsPagerAdapters
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
				if (listAdapter != null) {
					if (hostGroupChanged)
						listAdapter.clear();
					listAdapter.addAll(events);
					listAdapter.notifyDataSetChanged();
				}

				if (detailsAdapter != null) {
					if (hostGroupChanged)
						detailsAdapter.clear();
					detailsAdapter.addAll(events);
					detailsAdapter.notifyDataSetChanged();
				}

				if (callback != null)
					callback.onSeverityListAdapterLoaded(severity,
							hostGroupChanged);
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
	 * @param callback
	 *            listener to be called when the adapters have been updated
	 */
	public void loadProblemsBySeverityAndHostGroup(
			final TriggerSeverity severity, final long hostGroupId,
			final boolean hostGroupChanged,
			final OnSeverityListAdapterLoadedListener callback) {

		new RemoteAPITask(mRemoteAPI) {

			private List<Trigger> triggers;
			private final BaseServiceAdapter<Trigger> adapter = mProblemsListAdapters
					.get(severity);
			private final BaseServiceAdapter<Trigger> mainAdapter = mProblemsMainListAdapter;
			private final BaseSeverityPagerAdapter<Trigger> detailsAdapter = mProblemsDetailsPagerAdapters
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
								.getProblemsBySeverityAndHostGroupId(severity,
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
				if (mainAdapter != null && severity == TriggerSeverity.ALL
						&& hostGroupId == HostGroup.GROUP_ID_ALL) {
					mainAdapter.addAll(triggers);
					mainAdapter.notifyDataSetChanged();
				}
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

				if (callback != null)
					callback.onSeverityListAdapterLoaded(severity,
							hostGroupChanged);
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
			private final BaseServiceAdapter<HostGroup> groupsAdapter = mHostGroupsSpinnerAdapter;

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
	 * Additionally, this method initializes
	 * {@link ChecksApplicationsPagerAdapter}s (one for each host) if necessary.
	 * 
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapter will be cleared before being filled with entries
	 *            matching the selected host group.
	 * @param callback
	 *            listener to be called when the adapter has been updated
	 */
	public void loadHostsByHostGroup(final long hostGroupId,
			final boolean hostGroupChanged,
			final OnListAdapterLoadedListener callback) {
		new RemoteAPITask(mRemoteAPI) {

			private List<Host> hosts;
			private final BaseServiceAdapter<Host> hostsAdapter = mHostsListAdapter;

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
				if (callback != null)
					callback.onListAdapterLoaded();
				Log.d(TAG, "Hosts imported.");

			}

		}.execute();
	}

	/**
	 * Loads all applications with a given host group from the database
	 * asynchronously. After loading the events, the corresponding adapters are
	 * updated. If necessary, an import from the Zabbix API is triggered.
	 * 
	 * @param hostId
	 *            host id
	 * @param callback
	 *            Callback needed to trigger a redraw the view pager indicator
	 */
	public void loadApplicationsByHostId(final long hostId,
			final ChecksDetailsFragment callback) {
		new RemoteAPITask(mRemoteAPI) {

			List<Host> hosts;
			List<Application> applications;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				try {
					List<Long> hostIds = new ArrayList<Long>();
					hostIds.add(hostId);
					// We only import applications with corresponding hosts
					// (this way templates are ignored)
					mRemoteAPI.importApplicationsByHostIds(hostIds);
				} finally {
					try {
						applications = mDatabaseHelper
								.getApplicationsByHostId(hostId);

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// fill adapter
				if (mChecksApplicationsPagerAdapter != null) {
					mChecksApplicationsPagerAdapter.clear();
					mChecksApplicationsPagerAdapter.addAll(applications);
					mChecksApplicationsPagerAdapter.notifyDataSetChanged();
					// This is ugly, but we need it to redraw the page indicator
					if (callback != null)
						callback.redrawPageIndicator();
				}

			}

		}.execute();
	}

	/**
	 * Loads all items in a given application from the database asynchronously.
	 * After loading the events, the corresponding adapters are updated. An
	 * import from Zabbix is not necessary, because the items have already been
	 * loaded together with the applications. TODO: handle this case with cache
	 * functionality
	 * 
	 * @param hostGroupId
	 *            host group id by which the events will be filtered
	 * @param hostGroupChanged
	 *            whether the host group has changed. If this is true, the
	 *            adapters will be cleared before being filled with entries
	 *            matching the selected host group.
	 */
	public void loadItemsByApplicationId(final long applicationId) {
		new RemoteAPITask(mRemoteAPI) {

			List<Item> items;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				try {
					items = mDatabaseHelper
							.getItemsByApplicationId(applicationId);

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// fill adapters
				if (mChecksItemsListAdapter != null) {
					mChecksItemsListAdapter.clear();
					mChecksItemsListAdapter.addAll(items);
					mChecksItemsListAdapter.notifyDataSetChanged();
				}
				if (mChecksItemsPagerAdapter != null) {
					mChecksItemsPagerAdapter.clear();
					mChecksItemsPagerAdapter.addAll(items);
					mChecksItemsPagerAdapter.notifyDataSetChanged();
				}

			}

		}.execute();
	}

	/**
	 * Loads all history details for a given item. If necessary, an import from
	 * the Zabbix API is triggered.
	 * 
	 * @param itemId
	 *            item id
	 */
	public void loadHistoryDetailsByItemId(final long itemId, final OnHistoryDetailsLoadedListener callback) {
		new RemoteAPITask(mRemoteAPI) {

			List<HistoryDetail> historyDetails;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				try {
					// We only import applications with corresponding hosts
					// (this way templates are ignored)
					mRemoteAPI.importHistoryDetails(itemId);
				} finally {
					try {
						historyDetails = mDatabaseHelper
								.getHistoryDetailsByItemId(itemId);

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);

				if(callback != null)
					callback.onHistoryDetailsLoaded(historyDetails);
			}

		}.execute();
	}

	/**
	 * Acknowledges an event.
	 * 
	 * @param event
	 *            the event
	 * @param comment
	 *            an optional comment
	 * @param callback
	 *            callback to be notified when the acknowledgement was
	 *            successful
	 */
	public void acknowledgeEvent(final Event event, final String comment,
			final OnAcknowledgeEventListener callback) {
		new RemoteAPITask(mRemoteAPI) {

			private boolean mSuccess = false;

			@Override
			protected void executeTask() throws ZabbixLoginRequiredException,
					FatalException {
				mRemoteAPI.acknowledgeEvent(event.getId(), comment);
				try {
					mDatabaseHelper.acknowledgeEvent(event);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mSuccess = true;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (mSuccess && callback != null)
					callback.onEventAcknowledged();
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

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		bindings--;
		Log.d(TAG, "onUnbind: " + bindings);
		return super.onUnbind(intent);
	}

}
