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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.listeners.OnSeveritySelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Base class for a list fragment of a data type to be filtered by severity.
 * 
 * @param <T>
 *            the data type
 */
public abstract class BaseSeverityFilterListFragment<T> extends
		BaseServiceConnectedFragment {

	public static final String TAG = BaseSeverityFilterListFragment.class
			.getSimpleName();

	private static final String ARG_SPINNER_VISIBLE = "arg_spinner_visible";

	ViewPager mSeverityListPager;
	BaseSeverityListPagerAdapter<T> mSeverityListPagerAdapter;
	TabPageIndicator mSeverityListPageIndicator;

	private OnSeveritySelectedListener mCallbackMain;

	private boolean mProgressBarVisible = true;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallbackMain = (OnSeveritySelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSeveritySelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		if (savedInstanceState != null) {
			mProgressBarVisible = savedInstanceState.getBoolean(
					ARG_SPINNER_VISIBLE, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return inflater.inflate(R.layout.fragment_severity_list, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, "onViewCreated");
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		setupListViewPager();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_SPINNER_VISIBLE, mProgressBarVisible);
		super.onSaveInstanceState(outState);
	}

	private void setupListViewPager() {
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mSeverityListPager = (ViewPager) getView().findViewById(R.id.severity_list_viewpager);
		// Bind the tab indicator to the adapter
		mSeverityListPagerAdapter = retrievePagerAdapter();
		Log.d(TAG, "current severity: " + mSeverityListPagerAdapter.getCurrentObject());
		mSeverityListPagerAdapter.setFragmentManager(getChildFragmentManager());
		mSeverityListPager.setAdapter(mSeverityListPagerAdapter);
		mSeverityListPager.setOffscreenPageLimit(1);


	}

	protected abstract BaseSeverityListPagerAdapter<T> retrievePagerAdapter();

	/**
	 * Updates the severity currently selected in the view pager.
	 * 
	 * @param severity
	 *            the current severity
	 */
	public void setSeverity(TriggerSeverity severity) {
		mSeverityListPagerAdapter.setCurrentPosition(severity.getPosition());
	}

	/**
	 * Selects an item in the currently displayed list.
	 * 
	 * @param position
	 *            position of the item
	 */
	@SuppressWarnings("unchecked")
	public void selectItem(int position) {
		if (mSeverityListPagerAdapter == null
				|| mSeverityListPagerAdapter.getCount() == 0)
			return;
		BaseSeverityFilterListPage<T> currentPage = (BaseSeverityFilterListPage<T>) mSeverityListPagerAdapter
				.instantiateItem(mSeverityListPager,
						mSeverityListPager.getCurrentItem());
		Log.d(TAG, "selectItem(" + position + ")");
		if (currentPage != null)
			currentPage.selectItem(position);
	}

	/**
	 * Refreshes the item selection in the currently displayed list.
	 */
	@SuppressWarnings("unchecked")
	public void refreshItemSelection() {
		if (mSeverityListPager == null)
			return;
		BaseSeverityFilterListPage<T> currentPage = (BaseSeverityFilterListPage<T>) mSeverityListPagerAdapter
				.instantiateItem(mSeverityListPager,mSeverityListPager.getCurrentItem());
		if (currentPage != null)
			currentPage.refreshItemSelection();
	}

	/**
	 * Shows the progress bar indicating that the content is currently being
	 * loaded.
	 */
	public void showProgressBar() {
		mProgressBarVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.severity_list_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the progress bar.
	 */
	public void dismissProgressBar() {
		mProgressBarVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.severity_list_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
			ProgressBar listProgress = (ProgressBar) getView().findViewById(
					R.id.severity_list_progress);
			listProgress.setProgress(0);
		}
	}

	/**
	 * Updates the progress of the progress bar.
	 * 
	 * @param progress
	 *            current progress
	 */
	public void updateProgress(int progress) {
		if (getView() != null) {
			ProgressBar listProgress = (ProgressBar) getView().findViewById(
					R.id.severity_list_progress);
			listProgress.setProgress(progress);
		}
	}
}
