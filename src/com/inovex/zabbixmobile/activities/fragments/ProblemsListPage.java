package com.inovex.zabbixmobile.activities.fragments;

public class ProblemsListPage extends BaseSeverityFilterListPage {

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadTriggersBySeverityAndHostGroup(mSeverity,
					mHostGroupId, hostGroupChanged);
	}

	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getProblemsListAdapter(mSeverity));
	}

}
