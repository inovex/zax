package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.listeners.OnAcknowledgeEventListener;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Activity to visualize events.
 *
 */
public class EventsActivity extends BaseSeverityFilterActivity<Event> implements
		OnAcknowledgeEventListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		mTitle = getResources().getString(R.string.events);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Event>) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (BaseSeverityFilterListFragment<Event>) mFragmentManager
				.findFragmentById(R.id.events_list);
		if (mFlipper != null)
			Log.d(TAG, mFlipper.toString());
		Log.d(TAG, mListFragment.toString());
		Log.d(TAG, mDetailsFragment.toString());

		if (mFlipper != null)
			mDetailsFragment.setHasOptionsMenu(false);

		mDrawerToggle.setDrawerIndicatorEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mDrawerList.setItemChecked(BaseActivity.ACTIVITY_EVENTS, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mZabbixDataService != null) {
			mZabbixDataService.cancelLoadEventsTask();
			mZabbixDataService.cancelLoadHistoryDetailsTasks();
		}
	}

	@Override
	public void acknowledgeEvent(Event event, String comment) {
		Log.d(TAG, "acknowledgeEvent(" + event + ", " + comment + ")");
		mZabbixDataService.acknowledgeEvent(event, comment, this);
	}

	@Override
	public void onEventAcknowledged() {
		// this refreshes the action bar menu
		mDetailsFragment.refreshItemSelection();
		mDetailsFragment.refreshCurrentItem();
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		if (!mListFragment.isVisible())
			showListFragment();
	}

	@Override
	protected void showDetailsFragment() {
		super.showDetailsFragment();
		// details fragment becomes visible -> enable menu
		mDetailsFragment.setHasOptionsMenu(true);
	}

	@Override
	protected void showListFragment() {
		super.showListFragment();
		// details fragment becomes invisible -> disable menu
		if (mFlipper != null) {// portrait
			mDetailsFragment.setHasOptionsMenu(false);
		}
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		Log.d(TAG, "loadAdapterContent");
		super.loadAdapterContent(hostGroupChanged);
		mZabbixDataService.loadEventsByHostGroup(
				mSpinnerAdapter.getCurrentItemId(), hostGroupChanged, this);
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		super.onSeverityListAdapterLoaded(severity, hostGroupChanged);

		if (severity == mZabbixDataService.getEventsListPagerAdapter()
				.getCurrentObject()) {
			selectInitialItem(hostGroupChanged);
		}
	}

}
