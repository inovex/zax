package com.inovex.zabbixmobile.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverity;

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
	protected Fragment getPage(int position) {
		EventsDetailsPage f = new EventsDetailsPage();
		Event event = getItem(position);
		f.setEvent(event);
		return f;
	}

}