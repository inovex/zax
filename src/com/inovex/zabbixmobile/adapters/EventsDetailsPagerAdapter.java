package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Pager adapter used by {@link EventsDetailsFragment}.
 * 
 */
public class EventsDetailsPagerAdapter extends BaseSeverityPagerAdapter<Event> {

	private static final String TAG = EventsDetailsPagerAdapter.class
			.getSimpleName();

	public EventsDetailsPagerAdapter(TriggerSeverity severity) {
		super(severity);
	}

	public EventsDetailsPagerAdapter(FragmentManager fm,
			TriggerSeverity severity) {
		super(fm, severity);
	}

	@Override
	protected Fragment getItem(int position) {
		EventsDetailsPage f = new EventsDetailsPage();
		Event event = getObject(position);
		f.setEvent(event);
		return f;
	}

	@Override
	public long getItemId(int position) {
		return getObject(position).getId();
	}

}