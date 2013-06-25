package com.inovex.zabbixmobile.activities.fragments;

public class EventsListPage extends BaseSeverityFilterListPage {

	protected void loadAdapterContent(boolean hostGroupChanged) {
		if(mZabbixDataService != null)
			mZabbixDataService.loadEventsBySeverityAndHostGroup(mSeverity, mHostGroupId, hostGroupChanged);
	}

	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getEventsListAdapter(mSeverity));
	}

}
