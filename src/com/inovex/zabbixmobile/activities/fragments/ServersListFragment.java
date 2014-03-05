package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.listeners.OnServerSelectedListener;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersListFragment extends BaseServiceConnectedListFragment {

	private BaseServiceAdapter<ZabbixServer> mServersListAdapter;
	private OnServerSelectedListener mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mActivity = (OnServerSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnServerSelectedListener.");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mActivity.onServerSelected(mServersListAdapter.getItem(position));
	}

	@Override
	protected void setupListAdapter() {
		mServersListAdapter = mZabbixDataService
				.getServersListManagementAdapter();
		setListAdapter(mServersListAdapter);
	}

}
