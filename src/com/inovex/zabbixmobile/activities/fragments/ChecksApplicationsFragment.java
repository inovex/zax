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
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.ChecksApplicationsPagerAdapter;
import com.inovex.zabbixmobile.listeners.OnChecksItemSelectedListener;
import com.inovex.zabbixmobile.listeners.OnItemsLoadedListener;
import com.inovex.zabbixmobile.model.Host;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Fragment showing the applications of a particular host.
 * 
 * In particular, this includes a view pager with one page for each application.
 * Each page contains a list of items.
 * 
 */
public class ChecksApplicationsFragment extends BaseServiceConnectedFragment
		implements OnItemsLoadedListener {

	public static final String TAG = ChecksApplicationsFragment.class
			.getSimpleName();

	private Host mHost;
	private boolean mApplicationsProgressBarVisible = true;
	private boolean mItemsLoadingSpinnerVisible = true;

	private static final String ARG_APPLICATIONS_SPINNER_VISIBLE = "arg_applications_spinner_visible";
	private static final String ARG_ITEMS_SPINNER_VISIBLE = "arg_items_spinner_visible";

	private TextView mTitleView;

	protected ViewPager mApplicationsPager;
	protected TitlePageIndicator mApplicationsPageIndicator;
	protected ChecksApplicationsPagerAdapter mApplicationsPagerAdapter;

	private OnChecksItemSelectedListener mCallbackMain;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallbackMain = (OnChecksItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChecksItemSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mApplicationsProgressBarVisible = savedInstanceState.getBoolean(
					ARG_APPLICATIONS_SPINNER_VISIBLE, false);
			mItemsLoadingSpinnerVisible = savedInstanceState.getBoolean(
					ARG_ITEMS_SPINNER_VISIBLE, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_checks_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mTitleView = (TextView) view.findViewById(R.id.checks_title);
		if (mApplicationsProgressBarVisible)
			showApplicationsProgressBar();
		// if (mItemsLoadingSpinnerVisible)
		// showItemsLoadingSpinner();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ARG_APPLICATIONS_SPINNER_VISIBLE,
				mApplicationsProgressBarVisible);
		outState.putBoolean(ARG_ITEMS_SPINNER_VISIBLE,
				mItemsLoadingSpinnerVisible);
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		setupDetailsViewPager();
		if (mHost != null)
			mTitleView.setText("Host: " + mHost.getName());

	}

	/**
	 * Sets the host whose applications shall be shown.
	 * 
	 * @param h
	 *            the host
	 */
	public void setHost(Host h) {
		this.mHost = h;
		if (mHost != null && getView() != null)
			mTitleView.setText("Host: " + mHost.getName());
	}

	/**
	 * Performs the setup of the view pager used to swipe between details pages.
	 */
	protected void setupDetailsViewPager() {
		Log.d(TAG, "setupViewPager");

		retrievePagerAdapter();
		if (mApplicationsPagerAdapter != null) {
			mApplicationsPagerAdapter
					.setFragmentManager(getChildFragmentManager());

			// initialize the view pager
			mApplicationsPager = (ViewPager) getView().findViewById(
					R.id.checks_view_pager);
			mApplicationsPager.setAdapter(mApplicationsPagerAdapter);

			// Initialize the page indicator
			mApplicationsPageIndicator = (TitlePageIndicator) getView()
					.findViewById(R.id.checks_page_indicator);
			mApplicationsPageIndicator.setViewPager(mApplicationsPager);
			mApplicationsPageIndicator.setCurrentItem(0);
			mApplicationsPageIndicator
					.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

						@Override
						public void onPageScrollStateChanged(int arg0) {

						}

						@Override
						public void onPageScrolled(int arg0, float arg1,
								int arg2) {

						}

						@Override
						public void onPageSelected(int position) {
							Log.d(TAG, "detail page selected: " + position);

							selectApplication(position);

							// mDetailsPagerAdapter.getCurrentPage().selectItem(0);
							mCallbackMain.onApplicationSelected(position);

							showItemsLoadingSpinner();
							mZabbixDataService.loadItemsByApplicationId(
									mApplicationsPagerAdapter
											.getCurrentObject().getId(),
									ChecksApplicationsFragment.this);

						}

					});
		}
	}

	protected void retrievePagerAdapter() {
		mApplicationsPagerAdapter = mZabbixDataService
				.getChecksApplicationsPagerAdapter();
	}

	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showApplicationsProgressBar() {
		mApplicationsProgressBarVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.applications_progress_layout);
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
	public void dismissApplicationsProgressBar() {
		mApplicationsProgressBarVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.applications_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// This is a fix for the issue with the child fragment manager;
		// described here:
		// http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
		// and here: https://code.google.com/p/android/issues/detail?id=42601
		// If the fragment manager is not set to null, there will be issues when
		// the activity is destroyed and there are pending transactions
		mApplicationsPagerAdapter.setFragmentManager(null);
	}

	/**
	 * Shows a loading spinner instead of this page's list view.
	 */
	public void showItemsLoadingSpinner() {
		mItemsLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.items_progress_layout);
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
	public void dismissItemsLoadingSpinner() {
		mItemsLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.items_progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
			ProgressBar listProgress = (ProgressBar) getView().findViewById(
					R.id.applications_progress);
			listProgress.setProgress(0);
		}

	}

	@Override
	public void onItemsLoaded() {
		restoreItemSelection();
		dismissItemsLoadingSpinner();
	}

	/**
	 * Selects an application in the view pager.
	 * 
	 * @param position
	 */
	public void selectApplication(int position) {
		if (mApplicationsPagerAdapter != null) {
			mApplicationsPagerAdapter.setCurrentPosition(position);
			mApplicationsPager.setCurrentItem(position);
			mApplicationsPageIndicator.setCurrentItem(position);
		}
	}

	public void resetSelection() {
		mApplicationsPageIndicator.onPageSelected(0);
	}

	public void refreshSelection() {
		if (mApplicationsPagerAdapter == null
				|| mApplicationsPageIndicator == null)
			return;
		// mDetailsPageIndicator.invalidate();
		int position = mApplicationsPagerAdapter.getCurrentPosition();
		if (mApplicationsPageIndicator != null
				&& mApplicationsPagerAdapter.getCount() > position)
			mApplicationsPageIndicator.onPageSelected(position);
	}

	/**
	 * Selects an item in the currently displayed list.
	 * 
	 * @param position
	 *            item position
	 */
	public void selectItem(int position) {
		if (mApplicationsPagerAdapter == null
				|| mApplicationsPagerAdapter.getCount() == 0)
			return;
		ChecksApplicationsPage currentPage = (ChecksApplicationsPage) mApplicationsPagerAdapter
				.instantiateItem(mApplicationsPager,
						mApplicationsPager.getCurrentItem());
		Log.d(TAG, "selectItem(" + position + ")");
		if (currentPage != null)
			currentPage.selectItem(position);
	}

	/**
	 * Restores the item selection on the current page.
	 */
	public void restoreItemSelection() {
		// selectApplication(mApplicationPosition);
		if (mApplicationsPagerAdapter != null) {
			ChecksApplicationsPage currentPage = (ChecksApplicationsPage) mApplicationsPagerAdapter
					.instantiateItem(mApplicationsPager,
							mApplicationsPager.getCurrentItem());
			if (currentPage != null) {
				// currentPage.selectItem(mItemPosition);
				currentPage.restoreItemSelection();
			}
		}
	}

	/**
	 * Updates the progress bar.
	 * 
	 * @param progress
	 *            current progress
	 */
	public void updateProgress(int progress) {
		if (getView() != null) {
			ProgressBar listProgress = (ProgressBar) getView().findViewById(
					R.id.applications_progress);
			listProgress.setProgress(progress);
		}
	}

	/**
	 * Unchecks the currently selected list item.
	 */
	public void uncheckCurrentListItem() {
		if (mApplicationsPagerAdapter != null) {
			ChecksApplicationsPage currentPage = (ChecksApplicationsPage) mApplicationsPagerAdapter
					.instantiateItem(mApplicationsPager,
							mApplicationsPager.getCurrentItem());
			if (currentPage != null) {
				currentPage.uncheckCurrentItem();
			}
		}
	}

}
