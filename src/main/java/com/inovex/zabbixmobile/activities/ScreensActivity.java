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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ScreensDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ScreensListFragment;
import com.inovex.zabbixmobile.listeners.OnScreensItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnScreensLoadedListener;
import com.inovex.zabbixmobile.model.Screen;

/**
 * Activity to visualize screens.
 * 
 */
public class ScreensActivity extends BaseActivity implements
		OnScreensLoadedListener, OnScreensItemSelectedListener {

	private static final String TAG = ScreensActivity.class.getSimpleName();

	private static final int FLIPPER_LIST_FRAGMENT = 0;
	private static final int FLIPPER_DETAILS_FRAGMENT = 1;
	public static final String LIST_FRAGMENT = "ListFragment";
	public static final String DETAILS_FRAGMENT = "DetailsFragment";

	protected ViewGroup mFragmentContainer;
	protected FragmentManager mFragmentManager;
	protected ScreensListFragment mListFragment;
	protected ScreensDetailsFragment mDetailsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitle = getResources().getString(R.string.activity_screens);
		setContentView(R.layout.activity_screens);

		mFragmentManager = getSupportFragmentManager();
		mFragmentContainer = (ViewGroup) findViewById(R.id.fragment_container);
		mListFragment = (ScreensListFragment) mFragmentManager
				.findFragmentByTag(LIST_FRAGMENT);
		mDetailsFragment = (ScreensDetailsFragment) mFragmentManager
				.findFragmentByTag(DETAILS_FRAGMENT);
		if(mListFragment == null)
			mListFragment = new ScreensListFragment();
		if(mDetailsFragment == null)
			mDetailsFragment = new ScreensDetailsFragment();
		showListFragment();
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mDrawerToggle.setDrawerIndicatorEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNavigationView.getMenu().findItem(R.id.navigation_item_screens).setChecked(true);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		if (mZabbixDataService.isLoggedIn())
			loadData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mZabbixDataService != null)
			mZabbixDataService.cancelLoadGraphsTask();
	}

	@Override
	public void onScreenSelected(Screen screen) {
//		mToolbar.setSubtitle(screen.getName());
		mDetailsFragment.setScreen(screen);
		showDetailsFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mFragmentContainer != null && mDetailsFragment.isVisible()) {
				showListFragment();
				return true;
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void showListFragment() {
		if (mFragmentContainer != null) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.replace(R.id.fragment_container,mListFragment, LIST_FRAGMENT);
			ft.commit();
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	protected void showDetailsFragment() {
		if (mFragmentContainer != null) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.replace(R.id.fragment_container,mDetailsFragment, DETAILS_FRAGMENT);
			ft.addToBackStack(null);
			ft.commit();
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
	}

	@Override
	public void onScreensLoaded() {
		mListFragment.dismissLoadingSpinner();
	}

	@Override
	protected void loadData() {
		mListFragment.showLoadingSpinner();
		mZabbixDataService.loadScreens(this);
		mDetailsFragment.showProgressBar();
		mDetailsFragment.loadGraphs();
	}

}
