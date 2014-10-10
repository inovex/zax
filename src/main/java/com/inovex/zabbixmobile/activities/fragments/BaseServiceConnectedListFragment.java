package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;

/**
 * Same as {@link BaseServiceConnectedFragment}, just based on a list fragment
 * instead of a regular one.
 * 
 */
public abstract class BaseServiceConnectedListFragment extends
		SherlockListFragment implements ServiceConnection {

	public static final String TAG = BaseServiceConnectedListFragment.class
			.getSimpleName();

	protected ZabbixDataService mZabbixDataService;

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		// we need to do this after the view was created!!
		Intent intent = new Intent(getSherlockActivity(),
				ZabbixDataService.class);
		getSherlockActivity().getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		getSherlockActivity().getApplicationContext().unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected");
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();
		setupListAdapter();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixDataService = null;
	}

	/**
	 * Retrieves the list adapter from the data service.
	 */
	protected abstract void setupListAdapter();

}
