package com.inovex.zabbixmobile.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
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
import com.inovex.zabbixmobile.push.PushService;

public abstract class BaseActivity extends SherlockFragmentActivity implements
		ServiceConnection, OnLoginProgressListener {

	private static final int REQUEST_CODE_PREFERENCES = 12345;

	protected ZabbixDataService mZabbixDataService;

	protected ActionBar mActionBar;
	private LoginProgressDialogFragment mLoginProgress;

	private boolean mPreferencesClosed = false;

	private static final String TAG = BaseActivity.class.getSimpleName();

	/** Defines callbacks for service binding, passed to bindService() */
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		Log.d(TAG, "onServiceConnected: " + this.getLocalClassName() + " "
				+ this.toString());
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
		mZabbixDataService.setActivityContext(BaseActivity.this);
		if (!mZabbixDataService.isLoggedIn())
			mZabbixDataService.performZabbixLogin(this);

	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "onServiceDisconnected()");
		mZabbixDataService = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		PushService.startOrStopPushService(this);
		// if the preferences activity has been closed (and possibly preferences
		// have been changed), we start a relogin.
		if (mPreferencesClosed && mZabbixDataService != null) {
			mZabbixDataService.performZabbixLogin(this);
			mPreferencesClosed = false;
		}

	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		boolean useMockData = getIntent().getBooleanExtra(
				ZabbixDataService.EXTRA_IS_TESTING, false);
		intent.putExtra(ZabbixDataService.EXTRA_IS_TESTING, useMockData);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService();
		mActionBar = getSupportActionBar();

		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(true);
		}

		// (re-) instantiate progress dialog
		mLoginProgress = (LoginProgressDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(LoginProgressDialogFragment.TAG);
		if (mLoginProgress == null)
			mLoginProgress = LoginProgressDialogFragment.getInstance();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

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
		switch(item.getItemId()) {
		case R.id.menuitem_preferences:
			Intent intent = new Intent(getApplicationContext(),
					ZaxPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_CODE_PREFERENCES);
			return true;
		case R.id.menuitem_clear:
			refreshData();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void refreshData() {
		mZabbixDataService.clearAllData();
		loadData();
	}

	protected abstract void loadData();

	@Override
	public void onLoginStarted() {
		disableUI();
		if (!this.isFinishing())
			mLoginProgress.show(getSupportFragmentManager(),
					LoginProgressDialogFragment.TAG);
	}

	@Override
	public void onLoginFinished(boolean success) {
		if (mLoginProgress != null) {
			try {
				mLoginProgress.dismiss();
			} catch (NullPointerException e) {
				// in case the progress is not being shown right now, a
				// NullPointerException will be thrown, which we can safely
				// ignore.
			}
		}
		if (success) {
			Toast.makeText(this, R.string.zabbix_login_successful,
					Toast.LENGTH_LONG).show();
			refreshData();
			enableUI();
		}
	}

	protected abstract void disableUI();

	protected abstract void enableUI();

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");

		if (requestCode == REQUEST_CODE_PREFERENCES)
			mPreferencesClosed = true;
	}

	/**
	 * The login progress dialog.
	 * 
	 */
	public static class LoginProgressDialogFragment extends DialogFragment {

		public static final String TAG = LoginProgressDialogFragment.class
				.getSimpleName();

		public static LoginProgressDialogFragment getInstance() {
			return new LoginProgressDialogFragment();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			setRetainInstance(true);
			ProgressDialog loginProgress = new ProgressDialog(getActivity());
			loginProgress.setTitle(R.string.zabbix_login);
			loginProgress.setMessage(getResources().getString(
					R.string.zabbix_login_in_progress));
			loginProgress.setIndeterminate(true);
			return loginProgress;
		}

	}
}
