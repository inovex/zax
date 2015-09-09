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
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnSeverityListAdapterLoadedListener;
import com.inovex.zabbixmobile.listeners.OnSeveritySelectedListener;
import com.inovex.zabbixmobile.model.Sharable;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Base class for all activities containing data which can be filtered by its
 * severity.
 * 
 * This type of activity follows the master-detail flow with a list fragment on
 * the left, and details on the right.
 * 
 * @param <T>
 *            The type of data to be visualized
 */
public abstract class BaseSeverityFilterActivity<T extends Sharable> extends
		BaseHostGroupSpinnerActivity implements OnListItemSelectedListener,
		OnSeveritySelectedListener, OnSeverityListAdapterLoadedListener {

	private static final String TAG = BaseSeverityFilterActivity.class
			.getSimpleName();

	protected FragmentManager mFragmentManager;
	protected FrameLayout mFragmentContainer;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment<T> mListFragment;

	public int getmCurrentItem() {
		return mCurrentItem;
	}

	private int mCurrentItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We'll be using a spinner menu
/*		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);*/
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		Log.d(TAG, "onServiceConnected()");
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		selectInitialItem(true);
	}

	@Override
	public void onListItemSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + ", position: " + position);

		// Caution: details fragment must be shown before selectItem() is
		// called! Otherwise the "acknowledge event" button might be displayed
		// erroneously
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			//switch to details fragment
			if(mFragmentContainer != null){
				FragmentTransaction transaction = mFragmentManager.beginTransaction();
				transaction.replace(R.id.fragment_container, mDetailsFragment, "DetailsFragment");
				transaction.addToBackStack(null);
				transaction.commit();

				// disable drawer toggle
				mDrawerToggle.setDrawerIndicatorEnabled(false);
				// details fragment becomes visible -> enable menu
//				mDetailsFragment.setHasOptionsMenu(true);
			}
		} else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			// switch to current item in detail view

		}
		mCurrentItem = position;
		mDetailsFragment.selectItem(position);
//		showDetailsFragment();
//		selectItem(position);
	}

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		if (severity == null)
			return;
		mDetailsFragment.setSeverity(severity);
		selectInitialItem(false);
	}


	/**
	 * Displays the details fragment.
	 */
	protected void showDetailsFragment() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

		}
	}

	/**
	 * Displays the list fragment.
	 */
	protected void showListFragment() {
		if (mFragmentContainer != null) {
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container,mListFragment,"ListFragment");
			transaction.commit();

			// details fragment becomes invisible -> disable menu
			mDetailsFragment.setHasOptionsMenu(false);
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		mListFragment.dismissProgressBar();
		mDetailsFragment.dismissLoadingSpinner();
	}

	@Override
	public void onSeverityListAdapterProgressUpdate(int progress) {
		Log.d(TAG, "progress update: " + progress);
		mListFragment.updateProgress(progress);
	}

	/**
	 * Selects an item in both fragments. If the host group has been changed,
	 * the first item will be selected; otherwise, the saved position will be
	 * restored.
	 * 
	 * @param reset
	 *            whether the item position shall be reset to 0
	 */
	protected void selectInitialItem(boolean reset) {
		if (reset) {
			selectItem(0);
		} else {
			mDetailsFragment.refreshItemSelection();
			mListFragment.refreshItemSelection();
		}
	}

	protected void selectItem(int position) {
		mDetailsFragment.selectItem(position);
		mListFragment.selectItem(position);
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		mListFragment.showProgressBar();
		mDetailsFragment.showLoadingSpinner();
	}

	@Override
	protected void loadData() {
		super.loadData();
		loadAdapterContent(false);
	}

	@Override
	public void onBackPressed() {
		if (mDetailsFragment.isVisible() && !mListFragment.isVisible()) {
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				if(mDetailsFragment.isVisible() && !mListFragment.isVisible()){
					showListFragment();
					return true;
				}
		}
		return super.onOptionsItemSelected(item);
	}
}
