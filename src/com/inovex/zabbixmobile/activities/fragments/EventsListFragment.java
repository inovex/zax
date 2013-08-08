package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsListPagerAdapter;

/**
 * Fragment displaying several lists of events (one for each severity) using a
 * view pager (adapter: {@link BaseSeverityListPagerAdapter}).
 * 
 */
public class EventsListFragment extends BaseSeverityFilterListFragment {

	public static final String TAG = EventsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityListPagerAdapter createPagerAdapter() {
		return new EventsListPagerAdapter(getChildFragmentManager());
	}

}
