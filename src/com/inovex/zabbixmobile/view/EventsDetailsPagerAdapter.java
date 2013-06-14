package com.inovex.zabbixmobile.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.activities.fragments.OnEventSelectedListener;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverities;

public class EventsDetailsPagerAdapter extends FragmentStatePagerAdapter implements
		OnEventSelectedListener, IEventsListAdapter {

	private List<Event> events;

	public EventsDetailsPagerAdapter(FragmentManager fm, TriggerSeverities severity) {
		super(fm);
		Log.d(EventsDetailsFragment.TAG, "creating DetailsPagerAdapter for severity " + severity);
		
		events = new ArrayList<Event>();
	}

	@Override
	public Fragment getItem(int i) {
		EventsDetailsPage f = new EventsDetailsPage();
		Event event = events.get(i);
		f.setEvent(event);
		return f;
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public void onEventSelected(int position, TriggerSeverities severity,
			long id) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void addAll(Collection<? extends Event> events) {
		this.events.addAll(events);
	}

	public void clear() {
		events.clear();
	}

}