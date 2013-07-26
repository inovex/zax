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

public abstract class BaseSeverityFilterActivity<T> extends
		BaseHostGroupSpinnerActivity implements OnListItemSelectedListener,
		OnSeveritySelectedListener, OnSeverityListAdapterLoadedListener {

	private static final String TAG = BaseSeverityFilterActivity.class
			.getSimpleName();

	private static final int FLIPPER_LIST_FRAGMENT = 0;
	private static final int FLIPPER_DETAILS_FRAGMENT = 1;

	private static final String CURRENT_ITEM_POSITION = "current_item_position";
	private static final String CURRENT_SEVERITY = "current_severity";

	protected int mCurrentItemPosition = 0;
	protected long mCurrentItemId = 0;
	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	protected FragmentManager mFragmentManager;
	protected ViewFlipper mFlipper;
	protected BaseSeverityFilterDetailsFragment<T> mDetailsFragment;
	protected BaseSeverityFilterListFragment mListFragment;

	private boolean mFirstCallSelectHostGroup = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mCurrentItemPosition = savedInstanceState.getInt(
					CURRENT_ITEM_POSITION, 0);
			mSeverity = TriggerSeverity.getSeverityByNumber(savedInstanceState
					.getInt(CURRENT_SEVERITY, TriggerSeverity.ALL.getNumber()));
		}
		// We'll be using a spinner menu
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_ITEM_POSITION, mCurrentItemPosition);
		outState.putInt(CURRENT_SEVERITY, mSeverity.getNumber());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);
		Log.d(TAG, "onServiceConnected()");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item))
			return true;

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
		mListFragment.showLoadingSpinner();
		mDetailsFragment.showLoadingSpinner();
		super.selectHostGroupInSpinner(position, itemId);
		mListFragment.setHostGroupId(itemId);
		mDetailsFragment.setHostGroupId(itemId);
		if (!mFirstCallSelectHostGroup) {
			mDetailsFragment.redrawPageIndicator();
			mListFragment.selectItem(0);
			mDetailsFragment.selectItem(0);
			mFirstCallSelectHostGroup = false;
		}
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
		mDetailsFragment.selectItem(position);
		mListFragment.selectItem(position);

	}

	@Override
	public void onSeveritySelected(TriggerSeverity severity) {
		mSeverity = severity;
		mDetailsFragment.setSeverity(severity);
		mDetailsFragment.redrawPageIndicator();
		mListFragment.selectItem(0);
		mDetailsFragment.selectItem(0);
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

	@Override
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged) {
		mListFragment.dismissLoadingSpinner();
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
	 * @param hostGroupChanged
	 *            whether the host group has been changed
	 */
	protected void selectInitialItem(boolean hostGroupChanged) {
		if (hostGroupChanged) {
			mDetailsFragment.selectItem(0);
			mListFragment.selectItem(0);
			// TODO: maybe: save current item per severity and host group
			return;
		}
		mDetailsFragment.selectItem(mCurrentItemPosition);
		mListFragment.selectItem(mCurrentItemPosition);
	}

	@Override
	protected abstract void loadAdapterContent(boolean hostGroupChanged);

	@Override
	protected void loadData() {
		super.loadData();
		mListFragment.showLoadingSpinner();
		mDetailsFragment.showLoadingSpinner();
		loadAdapterContent(true);
	}
}
