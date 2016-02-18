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
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;

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
	protected static final String DETAILS_FRAGMENT = "DetailsFragment";
	protected static final String LIST_FRAGMENT = "ListFragment";

	protected FragmentManager mFragmentManager;
	protected ViewGroup mFragmentContainer;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment<T> mListFragment;
	private TriggerSeverity mSeverity = TriggerSeverity.ALL;

	public int getmCurrentItem() {
		return mCurrentItem;
	}

	private int mCurrentItem;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		mFragmentManager = getSupportFragmentManager();

		mListFragment = (BaseSeverityFilterListFragment<T>) mFragmentManager.findFragmentById(R.id.list_fragment);
		mDetailsFragment = (BaseSeverityFilterDetailsFragment<T>) mFragmentManager.findFragmentById(R.id.details_fragment);

		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

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

		if(mDetailsFragment == null || !mDetailsFragment.isVisible()){
			//start DetailsActivity
			Intent i = getDetailsIntent();
			Bundle extras = new Bundle();
			extras.putInt("severity",mSeverity.getPosition());
			extras.putInt("position",position);
			i.putExtras(extras);
			startActivity(i);


			// disable drawer toggle
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			// details fragment becomes visible -> enable menu
//			mDetailsFragment.setHasOptionsMenu(true);

		} else {
			// switch to current item in detail view
			mDetailsFragment.selectItem(position);
			if(mListFragment != null){
				mListFragment.selectItem(position);
			}
		}

		mCurrentItem = position;
	}

	protected abstract Intent getDetailsIntent();

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		if (severity == null)
			return;
		mSeverity = severity;
		if(mDetailsFragment != null){
			mDetailsFragment.setSeverity(severity);
		}
		selectInitialItem(false);
	}

	/**
	 * Displays the list fragment.
	 */
	protected void showListFragment() {
		if (mFragmentContainer != null) {
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			transaction.replace(R.id.fragment_container,mListFragment,"ListFragment");
			transaction.commitAllowingStateLoss();

			// details fragment becomes invisible -> disable menu
			if(mDetailsFragment != null){
				mDetailsFragment.setHasOptionsMenu(false);
			}
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		mListFragment.dismissProgressBar();
		mListFragment.refreshPagerTabStrip();
		if(mDetailsFragment != null){
			mDetailsFragment.dismissLoadingSpinner();
		}
	}

	@Override
	public void onSeverityListAdapterProgressUpdate(int progress) {
		if(progress % 10 == 0)
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
			if(mDetailsFragment != null && mDetailsFragment.isVisible())
				mDetailsFragment.refreshItemSelection();
			if(mListFragment.isVisible())
				mListFragment.refreshItemSelection();
		}
	}

	protected void selectItem(int position) {
		if(mDetailsFragment != null){
			mDetailsFragment.selectItem(position);
		}
		mListFragment.selectItem(position);
	}

	@Override
	protected void loadAdapterContent(boolean hostGroupChanged) {
		mListFragment.showProgressBar();
		mListFragment.refreshPagerTabStrip();
		if(mDetailsFragment != null){
			mDetailsFragment.showLoadingSpinner();
		}
	}

	@Override
	protected void loadData() {
		super.loadData();
		loadAdapterContent(false);
	}

	@Override
	public void onBackPressed() {
		if (mDetailsFragment != null && mDetailsFragment.isVisible() && !mListFragment.isVisible()) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				if(mDetailsFragment != null && mDetailsFragment.isVisible() && !mListFragment.isVisible()){
					showListFragment();
					return true;
				}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}
}
