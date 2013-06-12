package com.inovex.zabbixmobile.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment.Severities;
import com.inovex.zabbixmobile.activities.fragments.OnEventSelectedListener;
import com.inovex.zabbixmobile.model.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class EventsActivity extends SherlockFragmentActivity implements
		OnEventSelectedListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private DatabaseHelper databaseHelper = null;
	private FragmentManager fragmentManager;

	private int eventPosition;

	private int severity = Severities.ALL.getNumber();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		fragmentManager = getSupportFragmentManager();

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			FragmentTransaction ft = fragmentManager.beginTransaction();
			EventsListFragment listFragment = (EventsListFragment) fragmentManager
					.findFragmentByTag(EventsListFragment.TAG);
			if (listFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(listFragment);
			}
			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) fragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			if (detailsFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(detailsFragment);
			}

			ft.commit();
			fragmentManager.executePendingTransactions();

			ft = fragmentManager.beginTransaction();

			// add list and details fragment
			Bundle listArgs = new Bundle();
			listArgs.putInt(EventsListFragment.ARG_SEVERITY, severity);
			listArgs.putInt(EventsListFragment.ARG_EVENT_POSITION, eventPosition);
			listFragment = new EventsListFragment();
			ft.add(R.id.events_fragment_left, listFragment,
					EventsListFragment.TAG);

			detailsFragment = new EventsDetailsFragment();
			Bundle detailsArgs = new Bundle();
			detailsArgs.putInt(EventsDetailsFragment.ARG_EVENT_POSITION,
					eventPosition);
			detailsArgs.putInt(EventsDetailsFragment.ARG_SEVERITY, severity);
			detailsFragment.setArguments(detailsArgs);
			ft.add(R.id.events_fragment_right, new EventsDetailsFragment(),
					EventsDetailsFragment.TAG);
			ft.commit();
		} else {

			FragmentTransaction ft = fragmentManager.beginTransaction();
			EventsListFragment listFragment = (EventsListFragment) fragmentManager
					.findFragmentByTag(EventsListFragment.TAG);
			if (listFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(listFragment);
			}

			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) fragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			if (detailsFragment != null) {
				// if the fragment exists already, destroy it
				ft.remove(detailsFragment);
			}

			ft.commit();
			fragmentManager.executePendingTransactions();

			ft = fragmentManager.beginTransaction();
			ft.add(R.id.events_layout, new EventsListFragment(),
					EventsListFragment.TAG);
			ft.commit();
		}

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
	public void onEventSelected(int position, int severity, long id) {
		Log.d(TAG, "event selected: " + id + ",severity: " + severity
				+ "(position: " + position + ")");
		this.eventPosition = position;
		this.severity = severity;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			EventsDetailsFragment detailsFragment = (EventsDetailsFragment) fragmentManager
					.findFragmentByTag(EventsDetailsFragment.TAG);
			detailsFragment.selectEvent(position);
			
			EventsListFragment listFragment = (EventsListFragment) fragmentManager
					.findFragmentByTag(EventsListFragment.TAG);
			
		} else {
			EventsDetailsFragment f = new EventsDetailsFragment();
			Bundle args = new Bundle();
			args.putLong(EventsDetailsFragment.ARG_EVENT_ID, id);
			args.putInt(EventsDetailsFragment.ARG_EVENT_POSITION, position);
			args.putInt(EventsDetailsFragment.ARG_SEVERITY, severity);
			f.setArguments(args);
			
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.remove(fragmentManager.findFragmentByTag(EventsListFragment.TAG));
			ft.add(R.id.events_layout, f);
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	@Override
	public void onEventClicked(int position) {
		// TODO Auto-generated method stub

	}

}
