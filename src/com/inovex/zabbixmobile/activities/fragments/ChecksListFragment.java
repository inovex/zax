package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.HostGroup;

public class ChecksListFragment extends BaseServiceConnectedListFragment {

	public static String TAG = ChecksListFragment.class.getSimpleName();

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	private long mHostGroupId = HostGroup.GROUP_ID_ALL;

	private OnChecksItemSelectedListener mCallbackMain;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnChecksItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChecksItemSelectedListener.");
		}
	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	public void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	public void setHostGroup(long itemId) {
		this.mHostGroupId = itemId;
		loadAdapterContent(true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallbackMain.onHostSelected(position, id);
	}
	
	@Override
	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getHostsListAdapter());
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadHostsByHostGroup(mHostGroupId,
					hostGroupChanged);
	}

}
