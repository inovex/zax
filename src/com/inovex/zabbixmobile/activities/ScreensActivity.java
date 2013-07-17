package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ScreensDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ScreensListFragment;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnListItemsLoadedListener;

public class ScreensActivity extends BaseActivity implements
		OnListItemsLoadedListener, OnListItemSelectedListener {

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

		ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		mFragmentManager = getSupportFragmentManager();
		mFlipper = (ViewFlipper) findViewById(R.id.screens_flipper);
		mListFragment = (ScreensListFragment) mFragmentManager
				.findFragmentById(R.id.screens_list);
		mDetailsFragment = (ScreensDetailsFragment) mFragmentManager
				.findFragmentById(R.id.screens_details);
		showListFragment();

	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		mZabbixDataService.loadScreens(this);
	}

	@Override
	public void onListItemSelected(int position, long id) {
		mDetailsFragment.setScreenId(id);
		Log.d(TAG, "onListItemSelected(" + position + ", " + id + ")");
		showDetailsFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			try {
				finish();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (mFlipper != null) {
			if (mDetailsFragment.isVisible()) {
				showListFragment();
				return;
			}

		}
		super.onBackPressed();
	}

	protected void showListFragment() {
		if (mFlipper != null) {
			if (!mListFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_LIST_FRAGMENT);
			}
		}
	}

	protected void showDetailsFragment() {
		if (mFlipper != null) {
			if (!mDetailsFragment.isVisible())
				mFlipper.setDisplayedChild(FLIPPER_DETAILS_FRAGMENT);
		}
	}

	@Override
	public void onListItemsLoaded() {
		// TODO: load graphs
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

}
