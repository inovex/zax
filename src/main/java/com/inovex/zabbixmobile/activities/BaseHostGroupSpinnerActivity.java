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
import android.util.Log;
import android.widget.Toolbar;

import com.actionbarsherlock.app.ActionBar;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter.OnHostGroupSelectedListener;

/**
 * Base class for all activities having a host group spinner in the action bar.
 *
 */
public abstract class BaseHostGroupSpinnerActivity extends BaseActivity
		implements OnHostGroupSelectedListener {

	protected static final String TAG = BaseHostGroupSpinnerActivity.class
			.getSimpleName();

	protected HostGroupsSpinnerAdapter mSpinnerAdapter;

	private boolean mFirstCall = true;

	private Toolbar. mOnNavigationListener;

	protected class SpinnerNavigationListener implements
			ActionBar.OnNavigationListener {

		@Override
		public boolean onNavigationItemSelected(int position, long itemId) {
			// This method is called when the activity is created. This
			// means that during a configuration change, the saved state of
			// the spinner (selected item) might be overwritten. Hence, we
			// ignore the first call.
			Log.d(TAG, "onNavigationItemSelected(" + position + ", " + itemId
					+ ") " + "firstCall: " + mFirstCall);

			// avoid reset after orientation change / data refresh
			if (mFirstCall) {
				mFirstCall = false;
				if (position == 0)
					return true;
			}

			selectHostGroupInSpinner(position, itemId);
			return true;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		// refresh spinner title (necessary when we navigate here using the back
		// button)
		if (mSpinnerAdapter != null) {
			mSpinnerAdapter.setTitle(mTitle);
			mSpinnerAdapter.setCallback(this);
			mSpinnerAdapter.refreshSelection();
		}

		// reload adapter
		if (mZabbixDataService != null && mZabbixDataService.isLoggedIn())
			loadAdapterContent(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * Sets up the host group spinner and loads required data from Zabbix.
	 */
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);

		// set up spinner adapter
		mSpinnerAdapter = mZabbixDataService.getHostGroupSpinnerAdapter();
		mSpinnerAdapter.setCallback(this);

		mOnNavigationListener = new SpinnerNavigationListener();

		mActionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

		mSpinnerAdapter.setTitle(mTitle);
		mSpinnerAdapter.refreshSelection();

		if (mZabbixDataService.isLoggedIn())
			loadAdapterContent(false);
	}

	@Override
	public void onHostGroupSelected(int position) {
		try {
			mActionBar.setSelectedNavigationItem(position);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called when a host group in the spinner is selected. If
	 * additional actions (like updating fragments) have to be performed, this
	 * method needs to be overwritten.
	 *
	 * @param position
	 *            position of the selected host group
	 * @param itemId
	 *            id of the selected host group
	 */
	public void selectHostGroupInSpinner(int position, long itemId) {
		mSpinnerAdapter.setCurrentPosition(position);
		mSpinnerAdapter.notifyDataSetChanged();
		loadAdapterContent(true);
	}

	@Override
	public void refreshData() {
		mFirstCall = true;
		super.refreshData();
	}

	protected void loadAdapterContent(boolean hostGroupChanged) {

	}

	@Override
	protected void loadData() {
		// "simulate" a first call such that the host group selection is not
		// altered.
		// This would happen because the host group adapter is emptied and
		// refilled.
		if (mSpinnerAdapter.getCurrentPosition() != 0)
			mFirstCall = true;
	}

	@Override
	protected void onNavigationDrawerClosed() {
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		super.onNavigationDrawerClosed();
	}

	@Override
	protected void onNavigationDrawerOpened() {
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		mActionBar.setDisplayShowTitleEnabled(true);
		// set title
		super.onNavigationDrawerOpened();
	}

}
