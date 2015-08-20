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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.inovex.zabbixmobile.R;
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

	private Spinner.OnItemSelectedListener mOnNavigationListener;
	private Spinner mSpinner;

	protected class SpinnerNavigationListener implements
			Spinner.OnItemSelectedListener {

	/*	@Override
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
		}*/

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long itemId) {
			Log.d(TAG, "onNavigationItemSelected(" + position + ", " + itemId
					+ ") " + "firstCall: " + mFirstCall);

			// avoid reset after orientation change / data refresh
			if (mFirstCall) {
				mFirstCall = false;
				return;
			}

			selectHostGroupInSpinner(position, itemId);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
				mToolbar, false);
		android.support.v7.widget.Toolbar.LayoutParams lp = new android.support.v7.widget.Toolbar.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mToolbar.addView(spinnerContainer, lp);




/*		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);*/
		// refresh spinner title (necessary when we navigate here using the back
		// button)
		if (mSpinnerAdapter != null) {
			mSpinnerAdapter.setTitle(mTitle);
			mSpinnerAdapter.setCallback(this);
			mSpinnerAdapter.refreshSelection();
		}

		Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
		spinner.setAdapter(mSpinnerAdapter);

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

/*		mToolbar.setnav(mSpinnerAdapter,
				mOnNavigationListener);*/

		mSpinnerAdapter.setTitle(mTitle);
		mSpinnerAdapter.refreshSelection();

		if (mZabbixDataService.isLoggedIn())
			loadAdapterContent(false);
	}

	@Override
	public void onHostGroupSelected(int position) {
		try {
			//mActionBar.setSelectedNavigationItem(position);
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
/*		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);*/
		super.onNavigationDrawerClosed();
	}

	@Override
	protected void onNavigationDrawerOpened() {
/*		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		mActionBar.setDisplayShowTitleEnabled(true);*/
		// set title
		super.onNavigationDrawerOpened();
	}

}
