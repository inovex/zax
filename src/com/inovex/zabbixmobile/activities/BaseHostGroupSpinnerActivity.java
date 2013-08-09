package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter.OnHostGroupSelectedListener;

public abstract class BaseHostGroupSpinnerActivity extends BaseActivity
		implements OnHostGroupSelectedListener {

	protected static final String TAG = BaseHostGroupSpinnerActivity.class
			.getSimpleName();

	protected String mSpinnerTitle;

	protected HostGroupsSpinnerAdapter mSpinnerAdapter;

	private boolean mFirstCall = true;

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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		super.onServiceConnected(className, binder);

		// set up spinner adapter
		mSpinnerAdapter = mZabbixDataService.getHostGroupSpinnerAdapter();
		mSpinnerAdapter.setCallback(this);

		ActionBar.OnNavigationListener mOnNavigationListener = new SpinnerNavigationListener();

		mActionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

		mSpinnerAdapter.setTitle(mSpinnerTitle);
		mSpinnerAdapter.notifyDataSetChanged();
		// selectHostGroupInSpinner(mHostGroupPosition, mHostGroupId);

		loadAdapterContent(false);
	}

	@Override
	public void onHostGroupSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
	}

	/**
	 * This method is called when a host group in the spinner is selected. The
	 * spinner state is maintained in {@link BaseHostGroupSpinnerActivity}. If
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
		loadAdapterContent(true);
	}

	@Override
	protected void refreshData() {
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

}
