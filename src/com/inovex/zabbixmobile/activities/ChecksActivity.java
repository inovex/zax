package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksItemsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnListAdapterFilledListener;
import com.inovex.zabbixmobile.model.Host;

public class ChecksActivity extends BaseHostGroupSpinnerActivity implements
		OnChecksItemSelectedListener, OnListAdapterFilledListener {

	private static final String TAG = ChecksActivity.class.getSimpleName();

	private static final int FLIPPER_HOST_LIST_FRAGMENT = 0;
	private static final int FLIPPER_APPLICATIONS_FRAGMENT = 1;
	private static final int FLIPPER_ITEM_DETAILS_FRAGMENT = 2;

	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected ChecksListFragment mHostListFragment;
	protected ChecksDetailsFragment mApplicationsFragment;
	protected ChecksItemsDetailsFragment mItemDetailsFragment;

	protected int mCurrentHostPosition;
	private long mCurrentHostId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checks);

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);

		mSpinnerTitle = getResources().getString(R.string.checks);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.checks_flipper);
		mHostListFragment = (ChecksListFragment) mFragmentManager
				.findFragmentById(R.id.checks_list);
		mApplicationsFragment = (ChecksDetailsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mItemDetailsFragment = (ChecksItemsDetailsFragment) mFragmentManager
				.findFragmentById(R.id.checks_items_details);
		showHostListFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item)) return true;
		
		switch (item.getItemId()) {
		case android.R.id.home:
			if ((mApplicationsFragment.isVisible() || mItemDetailsFragment
					.isVisible()) && mFlipper != null) {
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
		this.mCurrentHostPosition = position;
		this.mCurrentHostId = id;

		mHostListFragment.selectItem(position);

		Host h = mZabbixDataService.getHostById(mCurrentHostId);
		// update view pager
		Log.d(TAG,
				"Retrieved host from database: "
						+ ((h == null) ? "null" : h.toString()));
		// mApplicationsFragment.selectHost(position, id);
		mApplicationsFragment.setHost(h);
		showApplicationsFragment();
		mZabbixDataService.loadApplicationsByHostId(id, mApplicationsFragment);

	}

	@Override
	public void onItemSelected(int position, long id) {
		mItemDetailsFragment.selectItem(position, id);
		showItemDetailsFragment();
	}

	@Override
	public void onBackPressed() {
		if (mFlipper != null) {
			if (mItemDetailsFragment.isVisible()) {
				showApplicationsFragment();
				return;
			}
			if (mApplicationsFragment.isVisible()) {
				showHostListFragment();
				return;
			}

		} else {
			if (mApplicationsFragment.isVisible()
					&& mItemDetailsFragment.isVisible()) {
				showHostListFragment();
				return;
			}

		}
		super.onBackPressed();
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);

		// mZabbixService.loadApplications();

	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		mHostListFragment.setHostGroup(itemId);
		if(!mHostListFragment.isVisible())
			showHostListFragment();
		// TODO: update details fragment
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
		if (mZabbixDataService != null)
			mZabbixDataService.loadHostsByHostGroup(mHostGroupId,
					hostGroupChanged, this);
	}

	protected void showHostListFragment() {
		if (mFlipper != null) {
			// portrait
			if (!mHostListFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_HOST_LIST_FRAGMENT);
			}
		} else {
			// landscape
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.show(mHostListFragment);
			ft.hide(mItemDetailsFragment);
			ft.commit();
		}
	}

	protected void showApplicationsFragment() {
		if (mFlipper != null) {
			// portrait
			if (!mApplicationsFragment.isVisible())
				mFlipper.setDisplayedChild(FLIPPER_APPLICATIONS_FRAGMENT);
		}
		// nothing to do for landscape: applications are always visible
	}

	protected void showItemDetailsFragment() {
		if (mFlipper != null) {
			// portrait
			if (!mItemDetailsFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_ITEM_DETAILS_FRAGMENT);
			}
		} else {
			// landscape
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.hide(mHostListFragment);
			ft.show(mItemDetailsFragment);
			ft.commit();
		}
	}

	@Override
	public void onListAdapterFilled() {
		mCurrentHostId = mHostListFragment.selectItem(mCurrentHostPosition);
		if (mCurrentHostId > 0) {
			Host h = mZabbixDataService.getHostById(mCurrentHostId);
			mApplicationsFragment.setHost(h);
			mZabbixDataService.loadApplicationsByHostId(h.getId(),
					mApplicationsFragment);
		}
	}

}
