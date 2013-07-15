package com.inovex.zabbixmobile.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public class ProblemsActivity extends BaseSeverityFilterActivity<Trigger> {

	private static final String TAG = EventsActivity.class.getSimpleName();
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_ITEM_POSITION = "item_position";
	/**
	 * Whether the details fragment shall be displayed on startup.
	 */
	private boolean mShowDetailsFragment = false;
	/**
	 * Whether the app shall nevigate back to the {@link MainActivity} when the
	 * back button is pressed.
	 */
	private boolean mBackToMain = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_problems);

		mSpinnerTitle = getResources().getString(R.string.problems);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.problems_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Trigger>) mFragmentManager
				.findFragmentById(R.id.problems_details);
		mListFragment = (BaseSeverityFilterListFragment) mFragmentManager
				.findFragmentById(R.id.problems_list);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			int position = extras.getInt(ARG_ITEM_POSITION, -1);
			long id = extras.getLong(ARG_ITEM_ID, -1);
			if (position != -1 && id != -1) {
				mShowDetailsFragment = true;
				mBackToMain = true;
//				mDetailsFragment.selectItem(position);
				mDetailsFragment.setPosition(position);
				showDetailsFragment();
			}
		}
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		// if the activity was started using the intent to display a particular
		// problem, we do not want to show the list fragment on startup
		if (!mListFragment.isVisible() && !mShowDetailsFragment)
			showListFragment();
		if(mShowDetailsFragment)
			mShowDetailsFragment = false;
	}

	@Override
	protected void showListFragment() {
		super.showListFragment();
		// When the list fragment is shown (due to selecting a different host
		// group or pressing the "up" button), the app shall not skip the list
		// fragment when the back button is pressed (this shall only be done
		// when selecting a problem via the intent and then pressing back from
		// the details view).
		mBackToMain = false;
	}

	@Override
	public void onBackPressed() {
		Bundle extras = getIntent().getExtras();
		// check if the activity was started from the main activity using intent
		// extras -> if so, we want to go back to the main activity
		if (mBackToMain)
			finish();
		super.onBackPressed();
	}

	@Override
	protected void disableUI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void enableUI() {
		// TODO Auto-generated method stub

	}

	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null) {
			for (TriggerSeverity severity : TriggerSeverity.values()) {
				mZabbixDataService.loadProblemsBySeverityAndHostGroup(severity,
						mHostGroupId, hostGroupChanged, this);
			}
		}
	}

}
