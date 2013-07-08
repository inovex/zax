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

public abstract class BaseActivity extends SherlockFragmentActivity implements
		ServiceConnection, OnLoginProgressListener {

	protected ZabbixDataService mZabbixDataService;
	
	protected ActionBar mActionBar;
	private LoginProgressDialogFragment mLoginProgress;
	
	private static final String TAG = BaseActivity.class.getSimpleName();

	/** Defines callbacks for service binding, passed to bindService() */
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		Log.d(TAG, "onServiceConnected: " + this.getLocalClassName() + " "
				+ this.toString());
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
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
	}

	/**
	 * Binds the Zabbix service.
	 */
	protected void bindService() {
		Intent intent = new Intent(this, ZabbixDataService.class);
		getApplicationContext().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService();
		mActionBar = getSupportActionBar();

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		
		// (re-) instantiate progress dialog
		mLoginProgress = (LoginProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(LoginProgressDialogFragment.TAG);
		if(mLoginProgress == null)
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
	public void onLoginStarted() {
		disableUI();
		mLoginProgress.show(getSupportFragmentManager(),
				LoginProgressDialogFragment.TAG);
	}

	@Override
	public void onLoginFinished(boolean success) {
		if (mLoginProgress != null) {
			mLoginProgress.dismiss();
		}
		if (success) {
			Toast.makeText(this, R.string.zabbix_login_successful,
					Toast.LENGTH_LONG).show();
			enableUI();
		}
	}

	protected abstract void disableUI();

	protected abstract void enableUI();

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
			// mLoginProgress.setCancelable(false);
			loginProgress.setIndeterminate(true);
			return loginProgress;
		}

	}
}
