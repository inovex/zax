package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.View;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Event;

/**
 * Fragment displaying a list of events for a particular severity.
 * 
 */
public class EventsListPage extends BaseSeverityFilterListPage<Event> {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setCustomEmptyText(getResources().getString(R.string.empty_list_events));
	}

	@Override
	protected void setupListAdapter() {
		mListAdapter = mZabbixDataService.getEventsListAdapter(mSeverity);
		setListAdapter(mListAdapter);
	}

}
