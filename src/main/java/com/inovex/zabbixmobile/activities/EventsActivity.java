/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
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

		mTitle = getResources().getString(R.string.activity_events);

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
		selectDrawerItem(ACTIVITY_EVENTS);
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

		BaseSeverityListPagerAdapter<Event> pagerAdapter = mZabbixDataService
				.getEventsListPagerAdapter();
		pagerAdapter.updateTitle(severity.getPosition(), mZabbixDataService
				.getEventsListAdapter(severity).getCount());
		mListFragment.refreshTabTitles();

		if (severity == mZabbixDataService.getEventsListPagerAdapter()
				.getCurrentObject()) {
			selectInitialItem(hostGroupChanged);
		}
	}

}
