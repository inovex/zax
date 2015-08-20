/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.widget.ViewFlipper;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.model.HostGroup;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Activity to visualize problems (triggers).
 * 
 */
public class ProblemsActivity extends BaseSeverityFilterActivity<Trigger> {

	public static final String ARG_TRIGGER_POSITION = "ARG_TRIGGER_POSITION";
	private static final String TAG = ProblemsActivity.class.getSimpleName();
	public static final String ARG_START_FROM_NOTIFICATION = "arg_start_from_notification";

	// This flag is necessary to select the correct element when coming from
	// the push notification
	private boolean mStartFromNotification = false;
	private int mTriggerPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_problems);

		mTitle = getResources().getString(R.string.activity_problems);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.problems_flipper);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<Trigger>) mFragmentManager
				.findFragmentById(R.id.problems_details);
		mListFragment = (BaseSeverityFilterListFragment<Trigger>) mFragmentManager
				.findFragmentById(R.id.problems_list);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getBoolean(ARG_START_FROM_NOTIFICATION, false)) {
				mStartFromNotification = true;
			}
			mTriggerPosition = extras.getInt(ARG_TRIGGER_POSITION, -1);
		}
		
		if (mFlipper != null)
			mDetailsFragment.setHasOptionsMenu(false);

		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		selectDrawerItem(ACTIVITY_PROBLEMS);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		// If the activity was started with intent extras, we have to select the
		// correct item.
		if (mStartFromNotification) {
			mDetailsFragment.setSeverity(TriggerSeverity.ALL);
			BaseSeverityListPagerAdapter<Trigger> severityAdapter = mZabbixDataService
					.getProblemsListPagerAdapter();
			severityAdapter.setCurrentPosition(0);
			selectHostGroupInSpinner(0, HostGroup.GROUP_ID_ALL);
			mStartFromNotification = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mZabbixDataService != null) {
			mZabbixDataService.cancelLoadProblemsTask();
			mZabbixDataService.cancelLoadHistoryDetailsTasks();
		}
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

		BaseSeverityListPagerAdapter<Trigger> pagerAdapter = mZabbixDataService
				.getProblemsListPagerAdapter();
		pagerAdapter.updateTitle(severity.getPosition(), mZabbixDataService
				.getProblemsListAdapter(severity).getCount());
		mListFragment.refreshTabTitles();

		if (severity == TriggerSeverity.ALL && mTriggerPosition != -1) {
			selectItem(mTriggerPosition);
			showDetailsFragment();
			mTriggerPosition = -1;
			return;
		}
		if (severity == mZabbixDataService.getProblemsListPagerAdapter()
				.getCurrentObject()) {
			selectInitialItem(hostGroupChanged);
		}
	}

}
