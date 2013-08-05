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
import com.inovex.zabbixmobile.activities.fragments.ChecksApplicationsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksItemsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.listeners.OnApplicationsLoadedListener;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnHostsLoadedListener;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.Item;

public class ChecksActivity extends BaseHostGroupSpinnerActivity implements
		OnChecksItemSelectedListener, OnHostsLoadedListener,
		OnApplicationsLoadedListener {

	private static final String TAG = ChecksActivity.class.getSimpleName();

	private static final int FLIPPER_HOST_LIST_FRAGMENT = 0;
	private static final int FLIPPER_APPLICATIONS_FRAGMENT = 1;
	private static final int FLIPPER_ITEM_DETAILS_FRAGMENT = 2;

	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected ChecksListFragment mHostListFragment;
	protected ChecksApplicationsFragment mApplicationsFragment;
	protected ChecksItemsFragment mItemDetailsFragment;

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
		mApplicationsFragment = (ChecksApplicationsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mItemDetailsFragment = (ChecksItemsFragment) mFragmentManager
				.findFragmentById(R.id.checks_items_details);
		showHostListFragment();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mZabbixDataService.cancelLoadApplicationsTask();
		mZabbixDataService.cancelLoadItemsTask();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item))
			return true;

		switch (item.getItemId()) {
		case android.R.id.home:
			if(mFlipper != null) {
				if ((mApplicationsFragment.isVisible() || mItemDetailsFragment
						.isVisible()) && mFlipper != null) {
					mFlipper.showPrevious();
					return true;
				}	
			} else {
				if (mApplicationsFragment.isVisible()
						&& mItemDetailsFragment.isVisible()) {
					showHostListFragment();
					return true;
				}
			}
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
		mApplicationsFragment.showApplicationsProgressBar();
		showApplicationsFragment();
		mZabbixDataService.loadApplicationsByHostId(id, this);

	}

	@Override
	public void onItemSelected(int position, Item item) {
		mApplicationsFragment.selectItem(position);
		mItemDetailsFragment.setItem(item);
		showItemDetailsFragment();
	}

	@Override
	public void onApplicationSelected(int position) {
		// Selecting an application never makes the item details visible. If
		// item details are already visible, we select the first item of the
		// chosen application.
		if (mItemDetailsFragment.isVisible()) {
			mApplicationsFragment.selectItem(0);
			mItemDetailsFragment.setItem(null);
		}
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
		mHostListFragment.showLoadingSpinner();
		// this loads the adapter content, hence we need to show the loading
		// spinner first
		super.selectHostGroupInSpinner(position, itemId);
		mHostListFragment.setHostGroup(itemId);
		if (!mHostListFragment.isVisible())
			showHostListFragment();
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
	public void onHostsLoaded() {
		mCurrentHostId = mHostListFragment.selectItem(mCurrentHostPosition);
		if (mCurrentHostId > 0) {
			Host h = mZabbixDataService.getHostById(mCurrentHostId);
			mApplicationsFragment.setHost(h);
			mZabbixDataService.loadApplicationsByHostId(h.getId(), this);
		}
		mHostListFragment.dismissLoadingSpinner();
	}

	@Override
	public void onApplicationsLoaded() {
		// This is ugly, but we need it to redraw the page indicator
		mApplicationsFragment.redrawPageIndicator();
		mApplicationsFragment.dismissApplicationsProgressBar();
		mItemDetailsFragment.dismissLoadingSpinner();
	}

	@Override
	public void onApplicationsProgressUpdate(int progress) {
		mApplicationsFragment.updateProgress(progress);
	}

	@Override
	protected void loadData() {
		super.loadData();
		// This loads the host list and - if necessary (i.e. currentHostId is
		// set) - the applications.
		mHostListFragment.showLoadingSpinner();
		mApplicationsFragment.showApplicationsProgressBar();
		mItemDetailsFragment.showLoadingSpinner();
		loadAdapterContent(true);
	}

}
