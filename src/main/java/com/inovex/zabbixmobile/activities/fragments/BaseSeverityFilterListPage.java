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
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.BaseActivity;
import com.inovex.zabbixmobile.adapters.BaseServiceAdapter;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Represents one page of a list view pager. Shows a list of items
 * (events/problems) for a specific severity.
 * 
 * @param <T>
 *            the data type
 */
public abstract class BaseSeverityFilterListPage<T> extends
		BaseServiceConnectedListFragment {

	private static final String TAG = BaseSeverityFilterListPage.class
			.getSimpleName();

	private static final String ARG_SEVERITY = "arg_severity";

	private OnListItemSelectedListener mCallbackMain;

	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;

	protected BaseServiceAdapter<T> mListAdapter;
	private SwipeRefreshLayout swipeRefreshLayout;
	private int mLastFirstVisibleItem;
	private boolean mIsScrollingUp;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnListItemSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: " + this.toString());
		if (savedInstanceState != null) {
			mSeverity = TriggerSeverity.getSeverityByNumber(
					savedInstanceState.getInt(ARG_SEVERITY, TriggerSeverity.ALL.getNumber()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_severity_filter_list,
				null);
		swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				((BaseActivity)getActivity()).refreshData();
				swipeRefreshLayout.setRefreshing(false);
			}
		});

		final ListView listView = (ListView) rootView.findViewById(android.R.id.list);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				final ListView lw = getListView();

					if (view.getId() == lw.getId()) {
						final int currentFirstVisibleItem = lw.getFirstVisiblePosition();

						if (currentFirstVisibleItem > mLastFirstVisibleItem) {
							mIsScrollingUp = false;
						} else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
							mIsScrollingUp = true;
						}

					mLastFirstVisibleItem = currentFirstVisibleItem;
					}
				if(mIsScrollingUp){
					//TODO hide subtitle-toolbar on scroll
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0)
						? 0 : listView.getChildAt(0).getTop();
				swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
			}
		});
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_SEVERITY, mSeverity.getNumber());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, this + " onViewCreated");
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		if (getView() != null && getListAdapter() != null
				&& getListView() != null) {
			getListView().setItemChecked(mListAdapter.getCurrentPosition(),
					true);
			getListView().setSelection(mListAdapter.getCurrentPosition());
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(l, v, " + position + ", " + id
				+ "). severity: " + mSeverity);
		Log.d(TAG, "list item clicked in: " + this.toString());
		mCallbackMain.onListItemSelected(position, id);
	}

	/**
	 * Selects an item in this fragment's list view.
	 * 
	 * @param position
	 */
	public void selectItem(int position) {
		if (mListAdapter != null)
			mListAdapter.setCurrentPosition(position);
		// check if the view has already been created -> if not, calls will be
		// made in onServiceConnected().
		if (getView() != null && getListView() != null) {
			getListView().setItemChecked(position, true);
			getListView().setSelection(position);
		}
	}

	/**
	 * Updates the list view position using the current position saved in the
	 * list adapter.
	 */
	public void refreshItemSelection() {
		if (mListAdapter == null)
			return;
		int position = mListAdapter.getCurrentPosition();
		try{
			if (getListView() != null) {
				getListView().setItemChecked(position, true);
				getListView().setSelection(position);
			}
		} catch (IllegalStateException e){
			e.printStackTrace();
		}

	}

	/**
	 * Sets the severity of this page.
	 * 
	 * @param severity
	 */
	public void setSeverity(TriggerSeverity severity) {
		this.mSeverity = severity;
		Log.d(TAG, "setSeverity: " + severity.getNameResourceId() + " - "
				+ this.toString());
	}

	/**
	 * Retrieves this page's severity.
	 * 
	 * @return
	 */
	public TriggerSeverity getSeverity() {
		return mSeverity;
	}

	protected void setCustomEmptyText(CharSequence text) {
		TextView emptyView = (TextView) getView().findViewById(
				android.R.id.empty);
		if (emptyView != null)
			emptyView.setText(text);
	}

}
