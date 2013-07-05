package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.listeners.OnAcknowledgeEventListener;
import com.inovex.zabbixmobile.model.Event;

public class EventsActivity extends BaseSeverityFilterActivity<Event> implements
		OnAcknowledgeEventListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		mSpinnerTitle = getResources().getString(R.string.events);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Event>) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (BaseSeverityFilterListFragment) mFragmentManager
				.findFragmentById(R.id.events_list);
		if (mFlipper != null)
			Log.d(TAG, mFlipper.toString());
		Log.d(TAG, mListFragment.toString());
		Log.d(TAG, mDetailsFragment.toString());
		System.out.println();
	}

	@Override
	public void acknowledgeEvent(long eventId, String comment) {
		Log.d(TAG, "acknowledgeEvent(" + eventId + ", " + comment + ")");
		mZabbixDataService.acknowledgeEvent(eventId, comment);
	}

	@Override
	protected void showDetailsFragment() {
		super.showDetailsFragment();
		// details fragment becomes visible -> enable menu
		mDetailsFragment.setHasOptionsMenu(true);
	}

	@Override
	protected void showListFragment() {
		super.showListFragment();
		// details fragment becomes invisible -> disable menu
		mDetailsFragment.setHasOptionsMenu(false);
	}

	@Override
	protected void disableUI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void enableUI() {
		// TODO Auto-generated method stub

	}

}
