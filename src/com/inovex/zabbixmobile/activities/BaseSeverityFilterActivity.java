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
import com.inovex.zabbixmobile.activities.fragments.OnSeverityListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.inovex.zabbixmobile.view.HostGroupsSpinnerAdapter;

public abstract class BaseSeverityFilterActivity<T> extends BaseActivity
		implements OnSeverityListItemSelectedListener {

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
		Log.d(TAG, "item selected: " + id + ",severity: " + severity
				+ "(position: " + position + ")");
		this.mCurrentItemPosition = position;
		this.mSeverity = severity;

		mDetailsFragment.selectItem(position, severity, id);
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
