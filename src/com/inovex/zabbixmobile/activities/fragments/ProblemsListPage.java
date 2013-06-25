package com.inovex.zabbixmobile.activities.fragments;

public class ProblemsListPage extends BaseSeverityFilterListPage {

	protected void loadAdapterContent(boolean hostGroupChanged) {
		if(mZabbixDataService != null)
			mZabbixDataService.loadTriggersBySeverityAndHostGroup(mSeverity, mHostGroupId, hostGroupChanged);
	}

	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getProblemsListAdapter(mSeverity));
	}

}
