package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.model.Event;

/**
 * Fragment displaying several lists of events (one for each severity) using a
 * view pager (adapter: {@link BaseSeverityListPagerAdapter}).
 * 
 */
public class EventsListFragment extends BaseSeverityFilterListFragment<Event> {

	public static final String TAG = EventsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityListPagerAdapter retrievePagerAdapter() {
		return mZabbixDataService.getEventsListPagerAdapter();
	}

}
