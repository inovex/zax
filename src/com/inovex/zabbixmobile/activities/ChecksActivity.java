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
import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksItemsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;

public class ChecksActivity extends BaseActivity implements
		OnChecksItemSelectedListener {

	private static final String TAG = ChecksActivity.class.getSimpleName();

	protected int mCurrentItemPosition;
	protected long mHostGroupId;
	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected ChecksDetailsFragment mDetailsFragment;
	protected ChecksItemsDetailsFragment mItemsDetailsFragment;
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
		mListFragment = (ChecksListFragment) mFragmentManager
				.findFragmentById(R.id.checks_list);
		mDetailsFragment = (ChecksDetailsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mItemsDetailsFragment = (ChecksItemsDetailsFragment) mFragmentManager
				.findFragmentById(R.id.checks_items_details);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if ((mDetailsFragment.isVisible() || mItemsDetailsFragment.isVisible()) && mFlipper != null) {
				mFlipper.showPrevious();
			} else
				finish();
			break;
		}
		return false;
	}

	@Override
	public void onHostSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + " position: " + position + ")");
		this.mCurrentItemPosition = position;

		mDetailsFragment.selectHost(position, id);
		if (mFlipper != null)
			mFlipper.showNext();

	}

	@Override
	public void onItemSelected(int position, long id) {
		mItemsDetailsFragment.selectItem(position, id);
		if (mFlipper != null)
			mFlipper.showNext();
	}

	@Override
	public void onBackPressed() {
		if ((mDetailsFragment.isVisible() || mItemsDetailsFragment.isVisible())
				&& mFlipper != null) {
			mFlipper.showPrevious();
		} else {
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

		// mZabbixService.loadApplications();

	}

}
