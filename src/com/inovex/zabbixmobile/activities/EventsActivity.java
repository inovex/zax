package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnEventSelectedListener;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.TriggerSeverities;

public class EventsActivity extends SherlockFragmentActivity implements
		OnEventSelectedListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private int mEventPosition;
	private TriggerSeverities mSeverity = TriggerSeverities.ALL;

	private ZabbixDataService mZabbixService;

	private FragmentManager mFragmentManager;
	
	private ViewFlipper mFlipper;
	private EventsDetailsFragment mDetailsFragment;
	private EventsListFragment mListFragment;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ZabbixDataBinder binder = (ZabbixDataBinder) service;
			mZabbixService = binder.getService();
			mZabbixService.setActivityContext(EventsActivity.this);

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, ZabbixDataService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		View rootView = super.onCreateView(name, context, attrs);
		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (EventsDetailsFragment) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (EventsListFragment) mFragmentManager
				.findFragmentById(R.id.events_list);
		return rootView;
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConnection);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			try {
				finish();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
		return false;
	}

	@Override
	public void onEventSelected(int position, TriggerSeverities severity,
			long id) {
		Log.d(TAG, "event selected: " + id + ",severity: " + severity
				+ "(position: " + position + ")");
		this.mEventPosition = position;
		this.mSeverity = severity;

		mDetailsFragment.selectEvent(position, severity, id);
		if (mFlipper != null)
			mFlipper.showNext();

	}

	@Override
	public void onBackPressed() {
		if (mDetailsFragment.isVisible() && mFlipper != null) {
			Log.d(TAG, "DetailsFragment is visible.");
			mFlipper.showPrevious();
		} else {
			Log.d(TAG, "DetailsFragment is not visible.");
			super.onBackPressed();
		}
	}

}
