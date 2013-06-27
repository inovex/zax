package com.inovex.zabbixmobile.activities.fragments;


import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Event;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class EventsDetailsFragment extends BaseSeverityFilterDetailsFragment<Event> {

	public static final String TAG = EventsDetailsFragment.class
			.getSimpleName();
	@Override
	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getEventsDetailsPagerAdapter(mSeverity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.events_details, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuitem_acknowledge_event:
			Log.d(TAG, "Acknowledge event: " + mCurrentItemId);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
