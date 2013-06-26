package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class ProblemsActivity extends BaseSeverityFilterActivity<Trigger> {

	private static final String TAG = EventsActivity.class.getSimpleName();
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_ITEM_POSITION = "item_position";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_problems);
	
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		
		mTitle = getResources().getString(R.string.problems);
	
		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.problems_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Trigger>) mFragmentManager
				.findFragmentById(R.id.problems_details);
		mListFragment = (BaseSeverityFilterListFragment) mFragmentManager
				.findFragmentById(R.id.problems_list);
		Bundle extras = getIntent().getExtras();
		int position = extras.getInt(ARG_ITEM_POSITION, -1);
		long id = extras.getLong(ARG_ITEM_ID, -1);
		if(position != -1 && id != -1) {
			mDetailsFragment.setPosition(position);
			mDetailsFragment.setCurrentItemId(id);
			mFlipper.showNext();
		}
	
	}
}
