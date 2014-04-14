package com.inovex.zabbixmobile.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ServersListFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.listeners.OnServerSelectedListener;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;
import com.inovex.zabbixmobile.push.PushService;

public class ServersActivity extends SherlockFragmentActivity implements OnServerSelectedListener, ServiceConnection {

	private static final String TAG = "ServersActivity";
	private ActionBar mActionBar;
	private ServersListFragment mListFragment;
	private FragmentManager mFragmentManager;

	protected ZabbixDataService mZabbixDataService;

	@SuppressLint("ValidFragment")
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		Log.d(TAG, "onServiceConnected: " + this.getLocalClassName() + " "
				+ this.toString());
		ZabbixDataBinder zabbixBinder = (ZabbixDataBinder) binder;
		mZabbixDataService = zabbixBinder.getService();
		mZabbixDataService.setActivityContext(this);
		mZabbixDataService.loadZabbixServers(); // sync
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.d(TAG, "onServiceDisconnected()");
		mZabbixDataService = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ZaxPreferences prefs = ZaxPreferences
				.getInstance(getApplicationContext());
		if (prefs.isDarkTheme())
			setTheme(R.style.AppThemeDark);
		else
			setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servers);

		bindService();

		mActionBar = getSupportActionBar();

		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(true);
		}

		mFragmentManager = getSupportFragmentManager();
		mListFragment = (ServersListFragment) mFragmentManager
				.findFragmentById(R.id.servers_list);
	}

	/**
	 * This unbinds the service if the application is exiting.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		if (isFinishing()) {
			Log.d(TAG, "unbindService");
			getApplicationContext().unbindService(this);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menuitem_add_server:
			openNewServerDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openNewServerDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.add_server);
		alert.setMessage("Type the name of the new server");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value.length()>0) {
					ZabbixServer server = mZabbixDataService.createNewZabbixServer(value);
					onServerSelected(server);
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.server_manager, menu);
		return true;
	}

	@Override
	public void onServerSelected(ZabbixServer server) {
		//mDetailsFragment.setServer(server);
		Intent intent = new Intent(this, ZabbixServerPreferenceActivity.class);
		intent.putExtra(ZabbixServerPreferenceActivity.ARG_ZABBIX_SERVER_ID, server.getId());
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int request, int result, Intent arg2) {
		if (request == 1) {
			if (result == ZabbixServerPreferenceActivity.PREFERENCES_CHANGED_SERVER ||
					result == ZabbixServerPreferenceActivity.PREFERENCES_CHANGED_PUSH) {
				PushService.startOrStopPushService(getApplicationContext());
			}
		}
	}
}
