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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksApplicationsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksHostsFragment;
import com.inovex.zabbixmobile.activities.fragments.ChecksItemsFragment;
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

	protected FragmentManager mFragmentManager;
	protected ChecksHostsFragment mHostListFragment;
	protected ChecksApplicationsFragment mApplicationsFragment;
	protected ChecksItemsFragment mItemDetailsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		mTitle = getResources().getString(R.string.activity_checks);
		setContentView(R.layout.activity_checks);

/*		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);*/

		mFragmentManager = getSupportFragmentManager();
		mHostListFragment = (ChecksHostsFragment) mFragmentManager
				.findFragmentById(R.id.checks_list);
		mApplicationsFragment = (ChecksApplicationsFragment) mFragmentManager
				.findFragmentById(R.id.checks_details);
		mItemDetailsFragment = (ChecksItemsFragment) mFragmentManager
				.findFragmentById(R.id.checks_items_details);
		showHostListFragment();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNavigationView.getMenu().findItem(R.id.navigation_item_checks).setChecked(true);
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
			if (!mHostListFragment.isVisible()) {
				if (mApplicationsFragment.isVisible()) {
					showHostListFragment();
					return true;
				}
				showApplicationsFragment();
				return true;
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onHostSelected(int position, long id) {
		Log.d(TAG, "host selected: " + id + " position: " + position + ")");

		mHostListFragment.selectItem(position);

		Host h = mZabbixDataService.getHostById(id);
		// update view pager
		Log.d(TAG,
				"Retrieved host from database: "
						+ ((h == null) ? "null" : h.toString()));
		mApplicationsFragment.setHost(h);
		mApplicationsFragment.showApplicationsProgressBar();
		showApplicationsFragment();
		mApplicationsFragment.selectApplication(0);
		mZabbixDataService.loadApplicationsByHostId(id, this, true);

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
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& mItemDetailsFragment.isVisible()) {
			mItemDetailsFragment.setItem(null);
			mApplicationsFragment.selectItem(0);
		}
	}

	@Override
	public void onBackPressed() {
		if (!mHostListFragment.isVisible()) {
			if (mApplicationsFragment.isVisible()) {
				showHostListFragment();
				return;
			}
			showApplicationsFragment();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		// mHostListFragment.showLoadingSpinner();
		// this loads the adapter content, hence we need to show the loading
		// spinner first
		super.selectHostGroupInSpinner(position, itemId);
		selectInitialHost(true);
		if (!mHostListFragment.isVisible())
			showHostListFragment();
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		if (mZabbixDataService != null)
			mZabbixDataService.loadHostsByHostGroup(
					mSpinnerAdapter.getCurrentItemId(), hostGroupChanged, this);
		mHostListFragment.showLoadingSpinner();
	}

	protected void showHostListFragment() {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.show(mHostListFragment);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// portrait
			ft.show(mApplicationsFragment);
		} else {
			// landscape
			ft.hide(mItemDetailsFragment);
		}
		ft.commit();
		// Uncheck the currently selected list item because the item fragment is
		// no longer visible.
		mApplicationsFragment.uncheckCurrentListItem();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mItemDetailsFragment.setHasOptionsMenu(false);
	}

	protected void showApplicationsFragment() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// portrait
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.hide(mHostListFragment);
			ft.show(mApplicationsFragment);
			ft.commit();
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			// details fragment becomes visible -> enable menu
			mItemDetailsFragment.setHasOptionsMenu(false);
		}
		// nothing to do for landscape: applications are always visible
	}

	protected void showItemDetailsFragment() {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.hide(mHostListFragment);
		ft.show(mItemDetailsFragment);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// portrait
			ft.hide(mApplicationsFragment);
		} else {
			// landscape
			ft.show(mItemDetailsFragment);
		}
		ft.commit();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		// details fragment becomes visible -> enable menu
		mItemDetailsFragment.setHasOptionsMenu(true);
	}

	@Override
	public void onHostsLoaded() {
		Host h = selectInitialHost(false);
		if(h != null){
			mZabbixDataService.loadApplicationsByHostId(h.getId(), this, false);
			mHostListFragment.dismissLoadingSpinner();
		}
	}

	private Host selectInitialHost(boolean reset) {
		Host h;
		if (reset)
			h = mHostListFragment.selectItem(0);
		else
			h = mHostListFragment.refreshItemSelection();
		mApplicationsFragment.setHost(h);
		return h;
	}

	@Override
	public void onApplicationsLoaded(boolean resetSelection) {
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
