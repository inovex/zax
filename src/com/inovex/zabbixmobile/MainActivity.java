package com.inovex.zabbixmobile;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.inovex.zabbixmobile.activities.ChecksActivity;
import com.inovex.zabbixmobile.activities.EventsActivity;
import com.inovex.zabbixmobile.activities.ScreensActivity;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.OnLoginProgressListener;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;

public class MainActivity extends SherlockFragmentActivity implements
		ServiceConnection, OnLoginProgressListener {

	protected static final String TAG = MainActivity.class.getSimpleName();

	private ZabbixDataService mZabbixService;
	private ProgressDialog mLoginProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		ListView listView = (ListView) findViewById(R.id.main_activities);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.activities,
				android.R.layout.simple_expandable_list_item_1);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1,
					int position, long arg3) {
				Intent intent = null;
				switch (position) {
				case 0:
					intent = new Intent(MainActivity.this, EventsActivity.class);
					break;
				case 1:
					intent = new Intent(MainActivity.this, ChecksActivity.class);
					break;
				case 2:
					intent = new Intent(MainActivity.this,
							ScreensActivity.class);
					break;
				default:
					return;
				}
				MainActivity.this.startActivity(intent);
			}
		});

		LinearLayout baseLayout = (LinearLayout) findViewById(R.id.layout_main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Defines callbacks for service binding, passed to bindService() */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixService = binder.getService();
		mZabbixService.setActivityContext(MainActivity.this);
		
		mZabbixService.performZabbixLogin(this);

	}
	
	@Override
	public void onLoginStarted() {
		mLoginProgress = new ProgressDialog(MainActivity.this);
		mLoginProgress.setTitle("Zabbix login");
		mLoginProgress.setMessage("Logging in to Zabbix. Please wait.");
		mLoginProgress.setCancelable(false);
		mLoginProgress.setIndeterminate(true);
		mLoginProgress.show();
	}
	
	@Override
	public void onLoginFinished() {
		if(mLoginProgress != null)
			mLoginProgress.dismiss();
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, ZabbixDataService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(this);
	}

}
