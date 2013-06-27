package com.inovex.zabbixmobile.activities.fragments;


import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Event;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class EventsDetailsFragment extends BaseSeverityFilterDetailsFragment<Event> {

	public static final String TAG = EventsDetailsFragment.class
			.getSimpleName();
	@Override
	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getEventsDetailsPagerAdapter(mSeverity);
	}

}
