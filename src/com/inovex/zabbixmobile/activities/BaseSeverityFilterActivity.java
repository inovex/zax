package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnSeverityListAdapterLoadedListener;
import com.inovex.zabbixmobile.listeners.OnSeveritySelectedListener;
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
public abstract class BaseSeverityFilterActivity<T> extends
		BaseHostGroupSpinnerActivity implements OnListItemSelectedListener,
		OnSeveritySelectedListener, OnSeverityListAdapterLoadedListener {

	private static final String TAG = BaseSeverityFilterActivity.class
			.getSimpleName();

	private static final int FLIPPER_LIST_FRAGMENT = 0;
	private static final int FLIPPER_DETAILS_FRAGMENT = 1;

	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment<T> mListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		Log.d(TAG, "onServiceConnected()");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home
				&& mDetailsFragment.isVisible() && mFlipper != null) {
			showListFragment();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		mDetailsFragment.redrawPageIndicator();
		selectInitialItem(true);
	}

	@Override
	public void onListItemSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + ", position: " + position);

		// Caution: details fragment must be shown before selectItem() is
		// called! Otherwise the "acknowledge event" button might be displayed
		// erroneously
		showDetailsFragment();
		selectItem(position);
	}

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		if (severity == null)
			return;
		mDetailsFragment.setSeverity(severity);
		mDetailsFragment.redrawPageIndicator();
		selectInitialItem(false);
	}

	@Override
	public void onBackPressed() {
		if (mDetailsFragment.isVisible() && mFlipper != null) {
			Log.d(TAG, "DetailsFragment is visible.");
			showListFragment();
		} else {
			Log.d(TAG, "DetailsFragment is not visible.");
			super.onBackPressed();
		}
	}

	/**
	 * Displays the details fragment.
	 */
	protected void showDetailsFragment() {
		if (mFlipper != null) {
			if (!mDetailsFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_DETAILS_FRAGMENT);
			}
			// disable drawer toggle
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
		// details fragment becomes visible -> enable menu
		mDetailsFragment.setHasOptionsMenu(true);
	}

	/**
	 * Displays the list fragment.
	 */
	protected void showListFragment() {
		if (mFlipper != null) {
			if (!mListFragment.isVisible()) {
				mFlipper.setDisplayedChild(FLIPPER_LIST_FRAGMENT);
			}
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
		// details fragment becomes invisible -> disable menu
		if (mFlipper != null) {// portrait
			mDetailsFragment.setHasOptionsMenu(false);
		}
	}

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		mListFragment.dismissProgressBar();
		mDetailsFragment.redrawPageIndicator();
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

}
