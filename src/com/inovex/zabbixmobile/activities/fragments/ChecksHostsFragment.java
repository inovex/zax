package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.HostsListAdapter;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.HostGroup;

/**
 * Fragment that shows a list of hosts.
 *
 */
public class ChecksHostsFragment extends BaseServiceConnectedListFragment {

	public static String TAG = ChecksHostsFragment.class.getSimpleName();

	private static final String ARG_POSITION = "arg_position";
	private static final String ARG_ITEM_ID = "arg_item_id";
	private static final String ARG_SPINNER_VISIBLE = "arg_spinner_visible";

	// TODO: move state to adapter
	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	private long mHostGroupId = HostGroup.GROUP_ID_ALL;
	private boolean mLoadingSpinnerVisible = true;

	private OnChecksItemSelectedListener mCallbackMain;

	private HostsListAdapter mListAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnChecksItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChecksItemSelectedListener.");
		}
	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	public void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	public void setHostGroup(long itemId) {
		this.mHostGroupId = itemId;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		setCurrentPosition(position);
		setCurrentItemId(id);
		mCallbackMain.onHostSelected(position, id);
	}

	/**
	 * Selects a particular host.
	 * @param position the host's position
	 * @return the host's ID
	 */
	public long selectItem(int position) {
		if (mListAdapter == null || mListAdapter.getCount() == 0)
			return -1;
		if (position > mListAdapter.getCount() - 1)
			position = 0;
		mCurrentPosition = position;
		// check if the view has already been created -> if not, calls will be
		// made in onViewCreated().
		if (getView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
		setCurrentItemId(getListAdapter().getItemId(position));
		return mCurrentItemId;
	}

	@Override
	protected void setupListAdapter() {
		this.mListAdapter = mZabbixDataService.getHostsListAdapter();
		setListAdapter(mListAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
			mCurrentItemId = savedInstanceState.getLong(ARG_ITEM_ID);
			mLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_SPINNER_VISIBLE, false);
		}
		Log.d(TAG, "pos: " + mCurrentPosition + "; id: " + mCurrentItemId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_checks_list, null);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mCurrentPosition, true);
		getListView().setSelection(mCurrentPosition);
		if (mLoadingSpinnerVisible)
			showLoadingSpinner();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_POSITION, mCurrentPosition);
		outState.putLong(ARG_ITEM_ID, mCurrentItemId);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showLoadingSpinner() {
		mLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.list_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissLoadingSpinner() {
		mLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.list_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

}
