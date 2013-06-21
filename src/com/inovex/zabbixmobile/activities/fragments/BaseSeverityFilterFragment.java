package com.inovex.zabbixmobile.activities.fragments;

import java.util.ArrayList;

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
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.viewpagerindicator.TabPageIndicator;

public class BaseSeverityFilterFragment extends SherlockFragment {
	
	public static final String TAG = BaseSeverityFilterFragment.class.getSimpleName();
	
	private int mCurrentPosition = 0;
	private long mCurrentEventId = 0;
	private TriggerSeverity mCurrentSeverity = TriggerSeverity.ALL;

	ViewPager mSeverityListPager;
	SeverityListPagerAdapter mSeverityListPagerAdapter;
	TabPageIndicator mSeverityListTabIndicator;

	class SeverityListPagerAdapter extends FragmentPagerAdapter {

		ArrayList<SeverityListPage> fragments = new ArrayList<SeverityListPage>();

		public SeverityListPagerAdapter(FragmentManager fm) {
			super(fm);
			SeverityListPage f;
			for (TriggerSeverity s : TriggerSeverity.values()) {
				f = new SeverityListPage();
				f.setSeverity(s);

				fragments.add(f);
			}
		}

		@Override
		public Fragment getItem(int i) {

			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return TriggerSeverity.values().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			SeverityListPage f = ((SeverityListPage) getItem(position));
			return f.getTitle();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_severity_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// if (savedInstanceState != null) {
		// mCurrentPosition = savedInstanceState.getInt(ARG_EVENT_POSITION, 0);
		// mCurrentEventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
		// mCurrentSeverity = savedInstanceState.getInt(ARG_SEVERITY,
		// TriggerSeverities.ALL.getNumber());
		// }

		setupListViewPager();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// outState.putInt(ARG_EVENT_POSITION, mCurrentPosition);
		// outState.putLong(ARG_EVENT_ID, mCurrentEventId);
		// outState.putInt(ARG_SEVERITY, mCurrentSeverity);
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

					}

					@Override
					public void onPageScrollStateChanged(int position) {
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}
				});

	}

	public void selectEvent(int position) {
		if (mSeverityListPager == null)
			return;
		SeverityListPage f = (SeverityListPage) mSeverityListPagerAdapter
				.instantiateItem(mSeverityListPager,
						mSeverityListPager.getCurrentItem());
		Log.d(TAG, "EventCategoryFragment:selectEvent(" + position + ")");
		f.selectEvent(position);
		mCurrentPosition = position;
	}

	public void setCurrentPosition(int currentPosition) {
		this.mCurrentPosition = currentPosition;
	}

	public void setCurrentEventId(long currentEventId) {
		this.mCurrentEventId = currentEventId;
	}

	public void setCurrentSeverity(TriggerSeverity currentSeverity) {
		this.mCurrentSeverity = currentSeverity;
	}
}
