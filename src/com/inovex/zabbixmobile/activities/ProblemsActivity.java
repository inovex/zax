package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
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

	// This flag is necessary to select the correct element when coming from
	// another activity (e.g. Problems list in MainActivity)
	private boolean mFirstCallFromIntent = true;
	private int mItemPosition;
	private long mItemId;

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
			mItemPosition = extras.getInt(ARG_ITEM_POSITION, -1);
			mItemId = extras.getLong(ARG_ITEM_ID, -1);
			if (mItemPosition != -1 && mItemId != -1) {
				mFirstCallFromIntent = true;
				mShowDetailsFragment = true;
				mBackToMain = true;
				showDetailsFragment();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mZabbixDataService.cancelLoadProblemsTask();
		mZabbixDataService.cancelLoadHistoryDetailsTasks();
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		// if the activity was started using the intent to display a particular
		// problem, we do not want to show the list fragment on startup
		if (!mListFragment.isVisible() && !mShowDetailsFragment)
			showListFragment();
		if (mShowDetailsFragment)
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
			super.loadAdapterContent(hostGroupChanged);
			mZabbixDataService.loadProblemsByHostGroup(
					mSpinnerAdapter.getCurrentItemId(), hostGroupChanged, this);
		}
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		super.onSeverityListAdapterLoaded(severity, hostGroupChanged);

		// If the activity was started with intent extras, we have to select the
		// correct item.
		if (mFirstCallFromIntent && mItemId != -1
				&& mItemPosition != -1) {
			mListFragment.selectItem(mItemPosition);
			mDetailsFragment.selectItem(mItemPosition);
			mFirstCallFromIntent = false;
		} else {
			if (severity == mZabbixDataService.getProblemsListPagerAdapter()
					.getCurrentObject()) {
				selectInitialItem(hostGroupChanged);
			}
		}
	}

}
