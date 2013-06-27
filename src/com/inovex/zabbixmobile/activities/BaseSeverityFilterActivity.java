package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnListItemSelectedListener;
import com.inovex.zabbixmobile.activities.fragments.OnSeveritySelectedListener;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public abstract class BaseSeverityFilterActivity<T> extends BaseActivity
		implements OnListItemSelectedListener, OnSeveritySelectedListener {

	private static final String TAG = BaseSeverityFilterActivity.class
			.getSimpleName();

	protected int mCurrentItemPosition;
	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	protected long mHostGroupId;
	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment mListFragment;

	protected HostGroupsSpinnerAdapter mSpinnerAdapter;

	protected String mTitle;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);

		mSpinnerAdapter = mZabbixService.getHostGroupSpinnerAdapter();

		ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				mHostGroupId = itemId;
				mListFragment.setHostGroup(itemId);
				// TODO: update details fragment
				return true;
			}
		};

		mSpinnerAdapter.setTitle(mTitle);

		mActionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

		mZabbixService.loadHostGroups();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDetailsFragment.isVisible() && mFlipper != null) {
				showListFragment();
			} else {
				super.onBackPressed();
			}
			break;
		}
		return false;
	}

	@Override
	public void onListItemSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + ", position: " + position);
		this.mCurrentItemPosition = position;

		mDetailsFragment.selectItem(position, id);
		showDetailsFragment();

	}

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		mSeverity = severity;
		mDetailsFragment.setSeverity(severity);
	}

	@Override
	public void onBackPressed() {
		if (mDetailsFragment.isVisible() && mFlipper != null) {
			Log.d(TAG, "DetailsFragment is visible.");
			showListFragment();
		} else {
			Log.d(TAG, "DetailsFragment is not visible.");
			super.onBackPressed();
		}
	}

	protected void showDetailsFragment() {
		if (!mDetailsFragment.isVisible() && mFlipper != null) {
			mFlipper.showNext();
		}
		// details fragment becomes visible -> enable menu
		mDetailsFragment.setHasOptionsMenu(true);
	}

	protected void showListFragment() {
		if (!mListFragment.isVisible() && mFlipper != null) {
			mFlipper.showPrevious();
		}
		// details fragment becomes invisible -> disable menu
		mDetailsFragment.setHasOptionsMenu(false);
	}

}
