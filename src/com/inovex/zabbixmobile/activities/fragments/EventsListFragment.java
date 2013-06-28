package com.inovex.zabbixmobile.activities.fragments;

/**
 * Fragment displaying several lists of events (one for each severity) using a
 * view pager (adapter: {@link EventsListFragment.SeverityListPagerAdapter}).
 * 
 */
public class EventsListFragment extends BaseSeverityFilterListFragment {

	public static final String TAG = EventsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new EventsListPage();
	}

}
