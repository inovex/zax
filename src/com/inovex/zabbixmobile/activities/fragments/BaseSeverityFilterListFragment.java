package com.inovex.zabbixmobile.activities.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.listeners.OnSeveritySelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.viewpagerindicator.TabPageIndicator;

public abstract class BaseSeverityFilterListFragment extends SherlockFragment {

	public static final String TAG = BaseSeverityFilterListFragment.class
			.getSimpleName();

	private static final String ARG_POSITION = "arg_position";
	private static final String ARG_ITEM_ID = "arg_item_id";
	private static final String ARG_SEVERITY = "arg_severity";

	private int mCurrentPosition = 0;
	private long mCurrentItemId = 0;
	private TriggerSeverity mCurrentSeverity = TriggerSeverity.ALL;
	private long mCurrentHostGroup;

	ViewPager mSeverityListPager;
	SeverityListPagerAdapter mSeverityListPagerAdapter;
	TabPageIndicator mSeverityListTabIndicator;

	ArrayList<BaseSeverityFilterListPage> pages = new ArrayList<BaseSeverityFilterListPage>();

	private OnSeveritySelectedListener mCallbackMain;

	class SeverityListPagerAdapter extends FragmentPagerAdapter {

		public SeverityListPagerAdapter(FragmentManager fm) {
			super(fm);
			BaseSeverityFilterListPage f;
			for (TriggerSeverity s : TriggerSeverity.values()) {
				f = instantiatePage();
				f.setSeverity(s);

				pages.add(f);
			}
		}

		@Override
		public Fragment getItem(int i) {

			return pages.get(i);
		}

		@Override
		public int getCount() {
			return TriggerSeverity.values().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			BaseSeverityFilterListPage f = ((BaseSeverityFilterListPage) getItem(position));
			return f.getTitle();
		}
	}

	protected abstract BaseSeverityFilterListPage instantiatePage();

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
		setRetainInstance(true);
		if (savedInstanceState != null) {
			setCurrentPosition(savedInstanceState.getInt(ARG_POSITION, 0));
			setCurrentItemId(savedInstanceState.getLong(ARG_ITEM_ID, 0));
			setCurrentSeverity(TriggerSeverity
					.getSeverityByNumber(savedInstanceState.getInt(
							ARG_SEVERITY, TriggerSeverity.ALL.getNumber())));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_severity_list, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupListViewPager();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_POSITION, mCurrentPosition);
		outState.putLong(ARG_ITEM_ID, mCurrentItemId);
		outState.putInt(ARG_SEVERITY, mCurrentSeverity.getNumber());
		super.onSaveInstanceState(outState);
	}

	private void setupListViewPager() {
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mSeverityListPagerAdapter = new SeverityListPagerAdapter(
				getChildFragmentManager());
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

	public void selectItem(int position) {
		if (mSeverityListPager == null)
			return;
		BaseSeverityFilterListPage f = (BaseSeverityFilterListPage) mSeverityListPagerAdapter
				.instantiateItem(mSeverityListPager,
						mSeverityListPager.getCurrentItem());
		Log.d(TAG, "selectEvent(" + position + ")");
		f.selectItem(position);
		mCurrentPosition = position;
	}

	protected void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	protected void setCurrentItemId(long currentItemId) {
		this.mCurrentItemId = currentItemId;
	}

	protected void setCurrentSeverity(TriggerSeverity currentSeverity) {
		this.mCurrentSeverity = currentSeverity;
	}

	public void setHostGroupId(long itemId) {
		this.mCurrentHostGroup = itemId;
		for (BaseSeverityFilterListPage p : pages) {
			p.setHostGroupId(itemId);
		}
	}
}
