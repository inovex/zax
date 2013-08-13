package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.widget.ViewFlipper;

import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ScreensDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ScreensListFragment;
import com.inovex.zabbixmobile.listeners.OnListItemsLoadedListener;
import com.inovex.zabbixmobile.listeners.OnScreensItemSelectedListener;
import com.inovex.zabbixmobile.model.Screen;

public class ScreensActivity extends BaseActivity implements
		OnListItemsLoadedListener, OnScreensItemSelectedListener {

	private static final String TAG = ScreensActivity.class.getSimpleName();

	private static final int FLIPPER_LIST_FRAGMENT = 0;
	private static final int FLIPPER_DETAILS_FRAGMENT = 1;

	protected ViewFlipper mFlipper;
	protected FragmentManager mFragmentManager;
	protected ScreensListFragment mListFragment;
	protected ScreensDetailsFragment mDetailsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screens);
		
		mTitle = getResources().getString(R.string.screens);
		
		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.screens_flipper);
		mListFragment = (ScreensListFragment) mFragmentManager
				.findFragmentById(R.id.screens_list);
		mDetailsFragment = (ScreensDetailsFragment) mFragmentManager
				.findFragmentById(R.id.screens_details);
		showListFragment();
		mDrawerToggle.setDrawerIndicatorEnabled(true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mDrawerList.setItemChecked(BaseActivity.ACTIVITY_SCREENS, true);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		loadData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mZabbixDataService.cancelLoadGraphsTask();
	}

	@Override
	public void onScreenSelected(Screen screen) {
		mActionBar.setSubtitle(screen.getName());
		mDetailsFragment.setScreen(screen);
		showDetailsFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mFlipper != null && mDetailsFragment.isVisible()) {
				showListFragment();
				return true;
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mFlipper != null && mDetailsFragment.isVisible()) {
			showListFragment();
			return;
		}
		super.onBackPressed();
	}

	protected void showListFragment() {
		if (mFlipper != null) {
			if (!mListFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_LIST_FRAGMENT);
			}
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	protected void showDetailsFragment() {
		if (mFlipper != null) {
			if (!mDetailsFragment.isVisible())
				mFlipper.setDisplayedChild(FLIPPER_DETAILS_FRAGMENT);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
	}

	@Override
	public void onListItemsLoaded() {
		mListFragment.dismissLoadingSpinner();
	}

	@Override
	protected void disableUI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void enableUI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadData() {
		mListFragment.showLoadingSpinner();
		mZabbixDataService.loadScreens(this);
		mDetailsFragment.showProgressBar();
		mDetailsFragment.loadGraphs();
	}

}
