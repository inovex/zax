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
import com.inovex.zabbixmobile.activities.fragments.ChecksHostsFragment;
import com.inovex.zabbixmobile.listeners.OnApplicationsLoadedListener;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnHostsLoadedListener;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.Item;

/**
 * Activity showing hosts, applications corresponding to the hosts and,
 * ultimately, the items within the applications.
 * 
 */
public class ChecksActivity extends BaseHostGroupSpinnerActivity implements
		OnChecksItemSelectedListener, OnHostsLoadedListener,
		OnApplicationsLoadedListener {

	private static final String TAG = ChecksActivity.class.getSimpleName();

	private static final int FLIPPER_HOST_LIST_FRAGMENT = 0;
	private static final int FLIPPER_APPLICATIONS_FRAGMENT = 1;
	private static final int FLIPPER_ITEM_DETAILS_FRAGMENT = 2;

	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected ChecksHostsFragment mHostListFragment;
	protected ChecksApplicationsFragment mApplicationsFragment;
	protected ChecksItemsFragment mItemDetailsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_checks);

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);

		mTitle = getResources().getString(R.string.checks);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.checks_flipper);
		mHostListFragment = (ChecksHostsFragment) mFragmentManager
				.findFragmentById(R.id.checks_list);
		mApplicationsFragment = (ChecksApplicationsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mItemDetailsFragment = (ChecksItemsFragment) mFragmentManager
				.findFragmentById(R.id.checks_items_details);
		showHostListFragment();
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDrawerList.setItemChecked(BaseActivity.ACTIVITY_CHECKS, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mZabbixDataService != null) {
			mZabbixDataService.cancelLoadApplicationsTask();
			mZabbixDataService.cancelLoadItemsTask();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mFlipper != null) {
				if (mApplicationsFragment.isVisible()) {
					showHostListFragment();
					return true;
				}
				if (mItemDetailsFragment.isVisible()) {
					showApplicationsFragment();
					return true;
				}
			} else {
				if (mApplicationsFragment.isVisible()
						&& mItemDetailsFragment.isVisible()) {
					showHostListFragment();
					return true;
				}
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onHostSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + " position: " + position + ")");

		mHostListFragment.selectItem(position);

		Host h = mZabbixDataService.getHostById(id);
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
	public void onItemSelected(int position, Item item, boolean showItemDetails) {
		mItemDetailsFragment.setItem(item);
		mItemDetailsFragment.dismissLoadingSpinner();
		if (showItemDetails) {
			showItemDetailsFragment();
		}
		if (showItemDetails || mItemDetailsFragment.isVisible())
			mApplicationsFragment.selectItem(position);
	}

	@Override
	public void onApplicationSelected(int position) {
		// Selecting an application never makes the item details visible. If
		// item details are already visible, we select the first item of the
		// chosen application.
		if (mFlipper != null && mItemDetailsFragment.isVisible()) {
			// mApplicationsFragment.selectItem(0);
			// mItemDetailsFragment.setItem(null);
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
		// mHostListFragment.showLoadingSpinner();
		// this loads the adapter content, hence we need to show the loading
		// spinner first
		super.selectHostGroupInSpinner(position, itemId);
		selectInitialItem(true);
		if (!mHostListFragment.isVisible())
			showHostListFragment();
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadHostsByHostGroup(
					mSpinnerAdapter.getCurrentItemId(), hostGroupChanged, this);
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
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	protected void showApplicationsFragment() {
		if (mFlipper != null) {
			// portrait
			if (!mApplicationsFragment.isVisible())
				mFlipper.setDisplayedChild(FLIPPER_APPLICATIONS_FRAGMENT);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
		// nothing to do for landscape: applications are always visible
	}

	protected void showItemDetailsFragment() {
		if (mFlipper != null) {
			// portrait
			if (!mItemDetailsFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_ITEM_DETAILS_FRAGMENT);
			}
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		} else {
			// landscape
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.hide(mHostListFragment);
			ft.show(mItemDetailsFragment);
			ft.commit();
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
	}

	@Override
	public void onHostsLoaded() {
		Host h = selectInitialItem(false);
		mZabbixDataService.loadApplicationsByHostId(h.getId(), this);
		mHostListFragment.dismissLoadingSpinner();
	}

	private Host selectInitialItem(boolean reset) {
		Host h;
		if(reset)
			h = mHostListFragment.selectItem(0);
		else 
			h = mHostListFragment.refreshItemSelection();
		mApplicationsFragment.setHost(h);
		return h;
	}

	@Override
	public void onApplicationsLoaded() {
		// This is ugly, but we need it to redraw the page indicator
		mApplicationsFragment.redrawPageIndicator();
		mApplicationsFragment.restoreApplicationSelection();
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
		mItemDetailsFragment.setItem(null);
		loadAdapterContent(true);
	}

}
