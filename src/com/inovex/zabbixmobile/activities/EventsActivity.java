package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public class EventsActivity extends BaseActivity implements
		OnListItemSelectedListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private int mEventPosition;
	private TriggerSeverity mSeverity = TriggerSeverity.ALL;

	private FragmentManager mFragmentManager;

	private ViewFlipper mFlipper;
	private EventsDetailsFragment mDetailsFragment;
	private EventsListFragment mListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.events_flipper);
		mDetailsFragment = (EventsDetailsFragment) mFragmentManager
				.findFragmentById(R.id.events_details);
		mListFragment = (EventsListFragment) mFragmentManager
				.findFragmentById(R.id.events_list);

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
	public void onListItemSelected(int position, TriggerSeverity severity, long id) {
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
