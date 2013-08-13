package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public class ProblemsActivity extends BaseSeverityFilterActivity<Trigger> {

	private static final String TAG = EventsActivity.class.getSimpleName();
	public static final String ARG_START_FROM_NOTIFICATION = "arg_start_from_notification";

	// This flag is necessary to select the correct element when coming from
	// the push notification
	private boolean mStartFromNotification = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_problems);

		mTitle = getResources().getString(R.string.problems);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.problems_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Trigger>) mFragmentManager
				.findFragmentById(R.id.problems_details);
		mListFragment = (BaseSeverityFilterListFragment) mFragmentManager
				.findFragmentById(R.id.problems_list);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getBoolean(ARG_START_FROM_NOTIFICATION, false)) {
				mStartFromNotification = true;
			}
		}

		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDrawerList.setItemChecked(BaseActivity.ACTIVITY_PROBLEMS, true);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		// If the activity was started with intent extras, we have to select the
		// correct item.
		if (mStartFromNotification) {
			mDetailsFragment.setSeverity(TriggerSeverity.ALL);
			BaseSeverityListPagerAdapter severityAdapter = mZabbixDataService.getProblemsListPagerAdapter();
			severityAdapter.setCurrentPosition(0);
			selectHostGroupInSpinner(0, HostGroup.GROUP_ID_ALL);
			mStartFromNotification = false;
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
		if (!mListFragment.isVisible())
			showListFragment();
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

		if (severity == mZabbixDataService.getProblemsListPagerAdapter()
				.getCurrentObject()) {
			selectInitialItem(hostGroupChanged);
		}
	}

}
