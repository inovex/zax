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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
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
		mTitle = getResources().getString(R.string.activity_events);
		FragmentManager fm = getSupportFragmentManager();
		mListFragment =
				(BaseSeverityFilterListFragment<Event>)
						fm.findFragmentByTag(BaseSeverityFilterActivity.LIST_FRAGMENT);
		mDetailsFragment =
				(BaseSeverityFilterDetailsFragment<Event>)
						fm.findFragmentByTag(BaseSeverityFilterActivity.DETAILS_FRAGMENT);
		if(mListFragment == null)
			mListFragment = new EventsListFragment();
		if(mDetailsFragment == null)
			mDetailsFragment = new EventsDetailsFragment();
		setContentView(R.layout.activity_events);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNavigationView.getMenu().findItem(R.id.navigation_item_events).setChecked(true);
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
//		if (!mListFragment.isVisible())
//			showListFragment();
	}

	@Override
	protected Intent getDetailsIntent() {
		return new Intent(this, EventDetailsActivity.class);
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

		if (severity == mZabbixDataService.getEventsListPagerAdapter()
				.getCurrentObject()) {
			selectInitialItem(hostGroupChanged);
		}
	}

}
