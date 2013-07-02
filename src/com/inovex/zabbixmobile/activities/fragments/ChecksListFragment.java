package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.HostGroup;

public class ChecksListFragment extends SherlockListFragment implements
		ServiceConnection {

	public static String TAG = ChecksListFragment.class.getSimpleName();

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	private long mHostGroupId = HostGroup.GROUP_ID_ALL;

	private OnChecksItemSelectedListener mCallbackMain;

	private ZabbixDataService mZabbixDataService;

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

	@Override
	public void onStart() {
		super.onStart();
		// we need to do this after the view was created!!
		Intent intent = new Intent(getSherlockActivity(),
				ZabbixDataService.class);
		getSherlockActivity().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		getSherlockActivity().unbindService(this);
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
	public void onServiceConnected(ComponentName name, IBinder service) {
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();

		Log.d(TAG, "service connected: " + mZabbixDataService + " - binder: "
				+ binder);
		setupListAdapter();
		loadAdapterContent(true);

	}

	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadHostsByHostGroup(mHostGroupId,
					hostGroupChanged);
	}

	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getHostsListAdapter());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixDataService = null;
	}
}
