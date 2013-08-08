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

public abstract class BaseSeverityFilterListFragment extends BaseServiceConnectedFragment {

	public static final String TAG = BaseSeverityFilterListFragment.class
			.getSimpleName();

	private static final String ARG_POSITION = "arg_position";
	private static final String ARG_ITEM_ID = "arg_item_id";
	private static final String ARG_SEVERITY = "arg_severity";
	private static final String ARG_SPINNER_VISIBLE = "arg_spinner_visible";

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	private TriggerSeverity mCurrentSeverity = TriggerSeverity.ALL;
	private long mCurrentHostGroupId;

	ViewPager mSeverityListPager;
	BaseSeverityListPagerAdapter mSeverityListPagerAdapter;
	TabPageIndicator mSeverityListTabIndicator;

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
			setCurrentPosition(savedInstanceState.getInt(ARG_POSITION, 0));
			setCurrentItemId(savedInstanceState.getLong(ARG_ITEM_ID, 0));
			setCurrentSeverity(TriggerSeverity
					.getSeverityByNumber(savedInstanceState.getInt(
							ARG_SEVERITY, TriggerSeverity.ALL.getNumber())));
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
		outState.putInt(ARG_POSITION, mCurrentPosition);
		outState.putLong(ARG_ITEM_ID, mCurrentItemId);
		outState.putInt(ARG_SEVERITY, mCurrentSeverity.getNumber());
		outState.putBoolean(ARG_SPINNER_VISIBLE, mProgressBarVisible);
		super.onSaveInstanceState(outState);
	}

	private void setupListViewPager() {
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mSeverityListPagerAdapter = retrievePagerAdapter();
		mSeverityListPagerAdapter.setFragmentManager(getChildFragmentManager());
		mSeverityListPager = (ViewPager) getView().findViewById(
				R.id.severity_list_viewpager);
		mSeverityListPager.setAdapter(mSeverityListPagerAdapter);
		mSeverityListPager.setOffscreenPageLimit(1);

		// Bind the tab indicator to the adapter
		mSeverityListTabIndicator = (TabPageIndicator) getView().findViewById(
				R.id.severity_list_tabindicator);
		mSeverityListTabIndicator.setViewPager(mSeverityListPager);

		mSeverityListTabIndicator
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						Log.d(TAG, "EventCategoryFragment:onPageSelected("
								+ position + ")");
						// EventsListPage p = (EventsListPage)
						// mEventsListPagerAdapter
						// .instantiateItem(mEventsListPager,
						// mEventsListPager.getCurrentItem());
						// mCallbackMain.onCategorySelected(position,
						// p.getSelectedEventNumber());
						// categoryNumber = position;
						BaseSeverityFilterListPage currentPage = (BaseSeverityFilterListPage) mSeverityListPagerAdapter
								.getItem(position);

						mCallbackMain.onSeveritySelected(currentPage
								.getSeverity());

					}

					@Override
					public void onPageScrollStateChanged(int position) {
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}
				});

	}

	protected abstract BaseSeverityListPagerAdapter retrievePagerAdapter();

	public void selectItem(int position) {
		if (mSeverityListPagerAdapter == null
				|| mSeverityListPagerAdapter.getCount() == 0)
			return;
		BaseSeverityFilterListPage currentPage = (BaseSeverityFilterListPage) mSeverityListPagerAdapter
				.instantiateItem(mSeverityListPager,
						mSeverityListPager.getCurrentItem());
		Log.d(TAG, "selectItem(" + position + ")");
		currentPage.selectItem(position);
		mCurrentPosition = position;
	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	protected void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	protected void setCurrentSeverity(TriggerSeverity currentSeverity) {
		this.mCurrentSeverity = currentSeverity;
	}

	public void setHostGroupId(long itemId) {
		this.mCurrentHostGroupId = itemId;
	}

	public void showProgressBar() {
		mProgressBarVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.severity_list_progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

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

	public void updateProgress(int progress) {
		if (getView() != null) {
			ProgressBar listProgress = (ProgressBar) getView().findViewById(
					R.id.severity_list_progress);
			listProgress.setProgress(progress);
		}
	}

}
