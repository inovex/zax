package com.inovex.zabbixmobile.activities;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;

import com.actionbarsherlock.app.ActionBar;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter;
import com.inovex.zabbixmobile.adapters.HostGroupsSpinnerAdapter.OnHostGroupSelectedListener;
import com.inovex.zabbixmobile.model.HostGroup;

public abstract class BaseHostGroupSpinnerActivity extends BaseActivity
		implements OnHostGroupSelectedListener {

	protected static final String ARG_HOST_GROUP_POSITION = "arg_host_group_position";
	protected static final String ARG_HOST_GROUP_ID = "arg_host_group_id";

	protected long mHostGroupId = HostGroup.GROUP_ID_ALL;
	protected int mHostGroupPosition = 0;

	protected String mSpinnerTitle;

	protected HostGroupsSpinnerAdapter mSpinnerAdapter;

	protected class SpinnerNavigationListener implements
			ActionBar.OnNavigationListener {

		private boolean firstCall = true;

		@Override
		public boolean onNavigationItemSelected(int position, long itemId) {
			// This method is called when the activity is created. This
			// means that during a configuration change, the saved state of
			// the spinner (selected item) might be overwritten. Hence, we
			// ignore the first call.
			if (firstCall) {
				firstCall = false;
				return true;
			}

			selectHostGroupInSpinner(position, itemId);
			return true;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mHostGroupPosition = savedInstanceState.getInt(
					ARG_HOST_GROUP_POSITION, 0);
			mHostGroupId = savedInstanceState.getLong(ARG_HOST_GROUP_ID, 0);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_HOST_GROUP_POSITION, mHostGroupPosition);
		outState.putLong(ARG_HOST_GROUP_ID, mHostGroupId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		super.onServiceConnected(className, service);

		// set up spinner adapter
		mSpinnerAdapter = mZabbixDataService.getHostGroupSpinnerAdapter();
		mSpinnerAdapter.setCallback(this);

		ActionBar.OnNavigationListener mOnNavigationListener = new SpinnerNavigationListener();

		mActionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

		mSpinnerAdapter.setTitle(mSpinnerTitle);
		selectHostGroupInSpinner(mHostGroupPosition, mHostGroupId);

		mZabbixDataService.loadHostGroups();
	}

	// TODO: host group selection might trigger a switch from list to details
	// view

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
		mHostGroupId = itemId;
		mHostGroupPosition = position;
		mSpinnerAdapter.setCurrentPosition(position);
	}

}
