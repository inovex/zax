package com.inovex.zabbixmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ServersDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ServersListFragment;
import com.inovex.zabbixmobile.listeners.OnServerSelectedListener;
import com.inovex.zabbixmobile.model.ZabbixServer;
import com.inovex.zabbixmobile.model.ZaxPreferences;

public class ServersActivity extends SherlockFragmentActivity implements OnServerSelectedListener {

	private static final String TAG = "ServersActivity";
	private ActionBar mActionBar;
	private ViewFlipper mFlipper;
	private ServersDetailsFragment mDetailsFragment;
	private ServersListFragment mListFragment;
	private FragmentManager mFragmentManager;

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

		mActionBar = getSupportActionBar();

		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(true);
		}

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.servers_flipper);
		mDetailsFragment = (ServersDetailsFragment) mFragmentManager
				.findFragmentById(R.id.servers_details);
		mListFragment = (ServersListFragment) mFragmentManager
				.findFragmentById(R.id.servers_list);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onServerSelected(ZabbixServer server) {
		//mDetailsFragment.setServer(server);
		Intent intent = new Intent(this, ZabbixServerPreferenceActivity.class);
		Log.d(TAG, "bbb="+server.getId());
		intent.putExtra(ZabbixServerPreferenceActivity.ARG_ZABBIX_SERVER_ID, server.getId());
		startActivity(intent);
	}

}
