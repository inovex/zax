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
import com.inovex.zabbixmobile.listeners.OnSeveritySelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public abstract class BaseSeverityFilterActivity<T> extends
		BaseHostGroupSpinnerActivity implements OnListItemSelectedListener,
		OnSeveritySelectedListener {

	private static final String TAG = BaseSeverityFilterActivity.class
			.getSimpleName();

	private static final int FLIPPER_LIST_FRAGMENT = 0;
	private static final int FLIPPER_DETAILS_FRAGMENT = 1;

	protected int mCurrentItemPosition;
	protected long mCurrentItemId;
	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment mListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		Log.d(TAG, "onServiceConnected()");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDetailsFragment.isVisible() && mFlipper != null) {
				showListFragment();
			} else {
				super.onBackPressed();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void selectHostGroupInSpinner(int position, long itemId) {
		super.selectHostGroupInSpinner(position, itemId);
		mListFragment.setHostGroupId(itemId);
		mDetailsFragment.setHostGroupId(itemId);
	}

	@Override
	public void onListItemSelected(int position, long id) {
		Log.d(TAG, "item selected: " + id + ", position: " + position);
		this.mCurrentItemPosition = position;
		this.mCurrentItemId = id;

		// Caution: details fragment must be shown before selectItem() is
		// called! Otherwise the "acknowledge event" button might be displayed
		// erroneously
		showDetailsFragment();
		mDetailsFragment.selectItem(position, id);
		mListFragment.selectItem(position);

	}

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		mSeverity = severity;
		mDetailsFragment.setSeverity(severity);
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
		if (!mDetailsFragment.isVisible() && mFlipper != null) {
			mFlipper.setDisplayedChild(FLIPPER_DETAILS_FRAGMENT);
		}
	}

	/**
	 * Displays the list fragment.
	 */
	protected void showListFragment() {
		if (!mListFragment.isVisible() && mFlipper != null) {
			mFlipper.setDisplayedChild(FLIPPER_LIST_FRAGMENT);
		}
	}

}
