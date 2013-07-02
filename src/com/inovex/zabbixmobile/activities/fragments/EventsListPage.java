package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.R;

import android.os.Bundle;
import android.view.View;

public class EventsListPage extends BaseSeverityFilterListPage {

	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.empty_list_events));
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadEventsBySeverityAndHostGroup(mSeverity,
					mHostGroupId, hostGroupChanged);
	}

	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getEventsListAdapter(mSeverity));
	}

}
