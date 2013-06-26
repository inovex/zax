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
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.activities.fragments.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.view.HostGroupsSpinnerAdapter;

public class ChecksActivity extends BaseActivity implements
		OnListItemSelectedListener {

	private static final String TAG = ChecksActivity.class.getSimpleName();

	protected int mCurrentItemPosition;
	protected long mHostGroupId;
	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected ChecksDetailsFragment mDetailsFragment;
	protected ChecksListFragment mListFragment;

	protected HostGroupsSpinnerAdapter mSpinnerAdapter;

	protected String mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checks);

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);

		mTitle = getResources().getString(R.string.checks);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.checks_flipper);
		mDetailsFragment = (ChecksDetailsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mListFragment = (ChecksListFragment) mFragmentManager
				.findFragmentById(R.id.checks_list);

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
	public void onListItemSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + " position: " + position + ")");
		this.mCurrentItemPosition = position;

		// mDetailsFragment.selectItem(position, severity, id);
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

		mZabbixService.loadApplications();

	}

}
