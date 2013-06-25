package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.view.HostGroupsSpinnerAdapter;

public class EventsActivity extends BaseSeverityFilterActivity implements
		OnListItemSelectedListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private int mEventPosition;
	private TriggerSeverity mSeverity = TriggerSeverity.ALL;
	private long mHostGroupId;

	private FragmentManager mFragmentManager;

	private ViewFlipper mFlipper;
	private BaseSeverityFilterDetailsFragment mDetailsFragment;
	private EventsListFragment mListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (EventsListFragment) mFragmentManager
				.findFragmentById(R.id.events_list);

	}
	
	

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);
		
		HostGroupsSpinnerAdapter spinnerAdapter = mZabbixService.getHostGroupSpinnerAdapter();
		
		ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				mHostGroupId = itemId;
				mListFragment.setHostGroup(itemId);
				return true;
			}
		};
		
		spinnerAdapter.setTitle(getResources().getString(R.string.events));
		
		mActionBar.setListNavigationCallbacks(spinnerAdapter,
				mOnNavigationListener);
		
		mZabbixService.loadHostGroups();
		
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
	public void onListItemSelected(int position, TriggerSeverity severity,
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
