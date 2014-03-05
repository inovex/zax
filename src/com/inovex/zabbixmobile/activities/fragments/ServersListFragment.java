package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersListFragment extends BaseServiceConnectedListFragment {

	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;

	@Override
	protected void setupListAdapter() {
		mServersListAdapter = mZabbixDataService
				.getServersListManagementAdapter();
		setListAdapter(mServersListAdapter);
	}

}
