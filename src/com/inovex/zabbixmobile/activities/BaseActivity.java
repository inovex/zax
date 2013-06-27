package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;

public class BaseActivity extends SherlockFragmentActivity implements ServiceConnection {

	protected ZabbixDataService mZabbixService;
	
	protected ActionBar mActionBar;
	
	/** Defines callbacks for service binding, passed to bindService() */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixService = binder.getService();
		mZabbixService.setActivityContext(BaseActivity.this);

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService();
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		mActionBar = getSupportActionBar();

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		super.onCreate(arg0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(this);
	}
}
