package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.model.Event;

public class EventsActivity extends BaseSeverityFilterActivity<Event> {

	private static final String TAG = EventsActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);
	
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		
		mTitle = getResources().getString(R.string.events);
	
		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Event>) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (BaseSeverityFilterListFragment) mFragmentManager
				.findFragmentById(R.id.events_details_list);
	
	}

}
