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

package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.HostsListAdapter;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.model.Host;

/**
 * Fragment that shows a list of hosts.
 * 
 */
public class ChecksHostsFragment extends BaseServiceConnectedListFragment {

	public static String TAG = ChecksHostsFragment.class.getSimpleName();

	private static final String ARG_SPINNER_VISIBLE = "arg_spinner_visible";

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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallbackMain.onHostSelected(position, id);
	}

	/**
	 * Selects a particular host.
	 * 
	 * @param position
	 *            the host's position
	 * @return the host's ID
	 */
	public Host selectItem(int position) {
		if (mListAdapter == null || mListAdapter.getCount() == 0)
			return null;
		if (position > mListAdapter.getCount() - 1)
			position = 0;
		mListAdapter.setCurrentPosition(position);
		// check if the view has already been created -> if not, calls will be
		// made in onViewCreated().
		if (getListView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
		return mListAdapter.getItem(mListAdapter.getCurrentPosition());
	}

	/**
	 * Updates the list view position using the current position saved in the
	 * list adapter.
	 */
	public Host refreshItemSelection() {
		if (mListAdapter == null)
			return null;
		int position = mListAdapter.getCurrentPosition();
		if (getView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
		return mListAdapter.getItem(position);

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
			mLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_SPINNER_VISIBLE, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_checks_hosts, null);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (mLoadingSpinnerVisible)
			showLoadingSpinner();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_SPINNER_VISIBLE, mLoadingSpinnerVisible);
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
