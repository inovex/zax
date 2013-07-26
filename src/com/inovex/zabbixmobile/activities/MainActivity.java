package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.MainMenuFragment;
import com.inovex.zabbixmobile.activities.fragments.MainProblemsFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.listeners.OnSeverityListAdapterLoadedListener;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public class MainActivity extends BaseActivity implements
		OnSeverityListAdapterLoadedListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	protected MainMenuFragment mMenuFragment;
	protected MainProblemsFragment mProblemsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setHomeButtonEnabled(false);

		mMenuFragment = (MainMenuFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_menu);
		mProblemsFragment = (MainProblemsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_problems);

	}

	@Override
	protected void disableUI() {
		// mProblemsButton.setEnabled(false);
		// mListAdapter.setEnabled(false);
		mMenuFragment.disableUI();
		mProblemsFragment.disableUI();
	}

	@Override
	protected void enableUI() {
		// mProblemsButton.setEnabled(true);
		// mListAdapter.setEnabled(true);
		mMenuFragment.enableUI();
		mProblemsFragment.enableUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);

		if (mZabbixDataService.isLoggedIn()) {

			// mZabbixService.performZabbixLogin(this);

			loadData();
		}

	}

	@Override
	public void onLoginFinished(boolean success) {
		super.onLoginFinished(success);
		if (success)
			loadData();
	}

	@Override
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		boolean useMockData = getIntent().getBooleanExtra(
				ZabbixDataService.EXTRA_IS_TESTING, false);
		intent.putExtra(ZabbixDataService.EXTRA_IS_TESTING, useMockData);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		// check whether we're coming back from another activity; if that's the
		// case, we reload the problems because there might have been a refresh
		if (mZabbixDataService != null)
			loadData();
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		mProblemsFragment.dismissLoadingSpinner();

	}

	@Override
	protected void loadData() {
		mProblemsFragment.showLoadingSpinner();
		mZabbixDataService.loadProblemsByHostGroup(HostGroup.GROUP_ID_ALL,
				true, this);
	}

	@Override
	public void onSeverityListAdapterProgressUpdate(int progress) {
		Log.d(TAG, "progress: " + progress);
		mProblemsFragment.updateProgress(progress);
	}

}
