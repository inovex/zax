package com.inovex.zabbixmobile.activities.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.viewpagerindicator.TabPageIndicator;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.TriggerSeverities;

public class EventsListFragment extends SherlockFragment {
	
	public static final String TAG = EventsListFragment.class.getSimpleName();
	public static final String ARG_EVENT_POSITION = "event_position";
	public static final String ARG_EVENT_ID = "event_id";
	public static final String ARG_SEVERITY = "severity";
	
	private int mCurrentPosition = 0;
	private long mCurrentEventId = 0;
	private int mCurrentSeverity = TriggerSeverities.ALL.getNumber();
	
	ViewPager mEventsListPager;
	EventListPagerAdapter mEventsListPagerAdapter;
	TabPageIndicator mEventListTabIndicator;
	
	class EventListPagerAdapter extends FragmentStatePagerAdapter {

		ArrayList<EventsListPage> fragments = new ArrayList<EventsListPage>();

		public EventListPagerAdapter(FragmentManager fm) {
			super(fm);
			EventsListPage f;
			for (TriggerSeverities s : TriggerSeverities.values()) {
				f = new EventsListPage();
				Bundle args = new Bundle();
				args.putInt(EventsListPage.ARG_SEVERITY, s.getNumber());
				args.putString(EventsListPage.ARG_TITLE,
						s.getName());
				f.setArguments(args);
				
				fragments.add(f);
			}
		}

		@Override
		public Fragment getItem(int i) {

			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return TriggerSeverities.values().length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			EventsListPage f = ((EventsListPage) getItem(position));
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
		return inflater.inflate(R.layout.fragment_events_list, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_EVENT_POSITION, 0);
			mCurrentEventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
			mCurrentSeverity = savedInstanceState.getInt(ARG_SEVERITY, TriggerSeverities.ALL.getNumber());
		}

		Bundle args = getArguments();
		if (args != null) {
			mCurrentPosition = args.getInt(ARG_EVENT_POSITION, 0);
			mCurrentEventId = args.getLong(ARG_EVENT_ID, 0);
			mCurrentSeverity = args.getInt(ARG_SEVERITY, TriggerSeverities.ALL.getNumber());
		}
		
		setupListViewPager();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_EVENT_POSITION, mCurrentPosition);
		outState.putLong(ARG_EVENT_ID, mCurrentEventId);
		outState.putInt(ARG_SEVERITY, mCurrentSeverity);
		super.onSaveInstanceState(outState);
	}

	private void setupListViewPager() {
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mEventsListPagerAdapter = new EventListPagerAdapter(
				getChildFragmentManager());
		mEventsListPager = (ViewPager) getView().findViewById(
				R.id.events_list_viewpager);
		mEventsListPager.setAdapter(mEventsListPagerAdapter);
		mEventsListPager.setOffscreenPageLimit(1);

		// Bind the tab indicator to the adapter
		mEventListTabIndicator = (TabPageIndicator) getView().findViewById(
				R.id.events_list_tabindicator);
		mEventListTabIndicator.setViewPager(mEventsListPager);
		
		mEventListTabIndicator
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						Log.d(TAG,
								"EventCategoryFragment:onPageSelected("
										+ position + ")");
						EventsListPage p = (EventsListPage) mEventsListPagerAdapter
								.instantiateItem(mEventsListPager,
										mEventsListPager.getCurrentItem());
//						mCallbackMain.onCategorySelected(position,
//								p.getSelectedEventNumber());
//						categoryNumber = position;

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
		if (mEventsListPager == null)
			return;
		EventsListPage f = (EventsListPage) mEventsListPagerAdapter
				.instantiateItem(mEventsListPager,
						mEventsListPager.getCurrentItem());
		Log.d(TAG,
				"EventCategoryFragment:selectEvent(" + position + ")");
		f.selectEvent(position);
		mCurrentPosition = position;
	}

}
