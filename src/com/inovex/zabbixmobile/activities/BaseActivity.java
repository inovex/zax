package com.inovex.zabbixmobile.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.OnLoginProgressListener;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;

public abstract class BaseActivity extends SherlockFragmentActivity implements
		ServiceConnection, OnLoginProgressListener {

	protected ZabbixDataService mZabbixDataService;

	private ProgressDialog mLoginProgress;

	protected ActionBar mActionBar;

	private static final String TAG = BaseActivity.class.getSimpleName();

	/** Defines callbacks for service binding, passed to bindService() */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		Log.d(TAG, "onServiceConnected: " + this.getLocalClassName() + " " + this.toString());
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();
		mZabbixDataService.setActivityContext(BaseActivity.this);
		mZabbixDataService.performZabbixLogin(this);

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "onServiceDisconnected()");
	}

	@Override
	protected void onStart() {
		super.onStart();
//		bindService();
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		bindService();
		mActionBar = getSupportActionBar();

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
//		getApplicationContext().unbindService(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG , "onDestroy");
		if (isFinishing()) {
			Log.d(TAG, "unbindService");
			getApplicationContext().unbindService(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuitem_preferences) {
			Intent intent = new Intent(getApplicationContext(),
					ZaxPreferenceActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		return false;
	}

	@Override
	public void onLoginStarted() {
		disableUI();
		mLoginProgress = new ProgressDialog(BaseActivity.this);
		mLoginProgress.setTitle(R.string.zabbix_login);
		mLoginProgress.setMessage(getResources().getString(
				R.string.zabbix_login_in_progress));
		// mLoginProgress.setCancelable(false);
		mLoginProgress.setIndeterminate(true);
		mLoginProgress.show();
	}
	
	@Override
	public void onLoginFinished(boolean success) {
		if (mLoginProgress != null)
			mLoginProgress.dismiss();
		if (success) {
			Toast.makeText(this, R.string.zabbix_login_successful,
					Toast.LENGTH_LONG).show();
			enableUI();
		}
	}

	protected abstract void disableUI();

	protected abstract void enableUI();
}
