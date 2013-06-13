package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

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

	private FragmentManager mFragmentManager;

	private int mEventPosition;
	private TriggerSeverities mSeverity = TriggerSeverities.ALL;
	
	private ZabbixDataService mZabbixService;

	private boolean mZabbixServiceBound = false;
	
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			ZabbixDataBinder binder = (ZabbixDataBinder) service;
			mZabbixService = binder.getService();
			mZabbixServiceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mZabbixServiceBound = false;
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

		mFragmentManager = getSupportFragmentManager();

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			EventsListFragment listFragment = (EventsListFragment) mFragmentManager
					.findFragmentByTag(EventsListFragment.TAG);
			if (listFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(listFragment);
			}
			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) mFragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			if (detailsFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(detailsFragment);
			}

			ft.commit();
			mFragmentManager.executePendingTransactions();

			ft = mFragmentManager.beginTransaction();

			// add list and details fragment
			listFragment = new EventsListFragment();
			listFragment.setCurrentPosition(mEventPosition);
			listFragment.setCurrentSeverity(mSeverity);
			ft.add(R.id.events_fragment_left, listFragment,
					EventsListFragment.TAG);

			detailsFragment = new EventsDetailsFragment();
			detailsFragment.setPosition(mEventPosition);
			detailsFragment.setSeverity(mSeverity);
			ft.add(R.id.events_fragment_right, new EventsDetailsFragment(),
					EventsDetailsFragment.TAG);
			ft.commit();
		} else {

			FragmentTransaction ft = mFragmentManager.beginTransaction();
			EventsListFragment listFragment = (EventsListFragment) mFragmentManager
					.findFragmentByTag(EventsListFragment.TAG);
			if (listFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(listFragment);
			}

			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) mFragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			if (detailsFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(detailsFragment);
			}

			ft.commit();
			mFragmentManager.executePendingTransactions();

			ft = mFragmentManager.beginTransaction();
			ft.add(R.id.events_layout, new EventsListFragment(),
					EventsListFragment.TAG);
			ft.commit();
		}

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

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) mFragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			detailsFragment.selectEvent(position);

			EventsListFragment listFragment = (EventsListFragment) mFragmentManager
					.findFragmentByTag(EventsListFragment.TAG);

		} else {
			EventsDetailsFragment detailsFragment = new EventsDetailsFragment();
			detailsFragment.setEventId(id);
			detailsFragment.setPosition(position);
			detailsFragment.setSeverity(severity);

			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.remove(mFragmentManager
					.findFragmentByTag(EventsListFragment.TAG));
			ft.add(R.id.events_layout, detailsFragment);
			ft.addToBackStack(null);
			ft.commit();
		}
	}
	
}
