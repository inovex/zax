package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.Application;

public class ChecksDetailsPage extends SherlockListFragment implements ServiceConnection {

	private Application mApplication;
	private String mTitle = "";
	
	public static String TAG = ChecksDetailsPage.class.getSimpleName();

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	
	private ZabbixDataService mZabbixDataService;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.empty_list_checks));
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

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();

		Log.d(TAG, "service connected: " + mZabbixDataService + " - binder: "
				+ binder);
		setupListAdapter();
//		loadAdapterContent();

	}

	public void loadAdapterContent() {
		if (mZabbixDataService != null)
			mZabbixDataService.loadItemsByApplicationId(mApplication.getId());
	}

	protected void setupListAdapter() {
		setListAdapter(mZabbixDataService.getApplicationItemsListAdapter());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixDataService = null;
	}
	
	public void setApplication(Application app) {
		this.mApplication = app;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}
	
}
