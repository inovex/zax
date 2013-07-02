package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.View;

import com.inovex.zabbixmobile.R;

public class ProblemsListPage extends BaseSeverityFilterListPage {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.empty_list_problems));
	}
	
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
