package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.View;

import com.inovex.zabbixmobile.R;

public class EventsListPage extends BaseSeverityFilterListPage {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setCustomEmptyText(getResources().getString(R.string.empty_list_events));
	}

	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getEventsListAdapter(mSeverity));
	}

}
