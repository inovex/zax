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

public class EventsListFragment extends SherlockFragment {
	
	public static final String TAG = EventsListFragment.class.getSimpleName();
	public static final String ARG_EVENT_POSITION = "event_position";
	public static final String ARG_EVENT_ID = "event_id";
	public static final String ARG_SEVERITY = "severity";
	
	private int currentPosition = 0;
	private long currentEventId = 0;
	private int currentSeverity = Severities.ALL.getNumber();
	
	ViewPager mEventsListPager;
	EventListPagerAdapter mEventsListPagerAdapter;
	TabPageIndicator mEventListTabIndicator;
	
	public enum Severities {
		ALL("all", -1, 0),
		DISASTER("disaster", 5, 1),
		HIGH("high", 4, 2),
		AVERAGE("average", 3, 3),
		WARNING("warning", 2, 4),
		INFORMATION("information", 1, 5),
		NOT_CLASSIFIED("not classified", 0, 6);
		
		private final String name;
		private final int number;
		private final int position;
		
		Severities(String name, int n, int position) {
			this.name = name;
			number = n;
			this.position= position; 
		}
		
		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}

		public int getNumber() {
			return number;
		}
		
		public static Severities getSeverityByNumber(int n) {
			return ALL;
		}
		
	}

	class EventListPagerAdapter extends FragmentStatePagerAdapter {

		ArrayList<EventsListPage> fragments = new ArrayList<EventsListPage>();

		public EventListPagerAdapter(FragmentManager fm) {
			super(fm);
			EventsListPage f;
			for (Severities s : Severities.values()) {
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
			return Severities.values().length;
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
			currentPosition = savedInstanceState.getInt(ARG_EVENT_POSITION, 0);
			currentEventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
			currentSeverity = savedInstanceState.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
		}

		Bundle args = getArguments();
		if (args != null) {
			currentPosition = args.getInt(ARG_EVENT_POSITION, 0);
			currentEventId = args.getLong(ARG_EVENT_ID, 0);
			currentSeverity = args.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
		}
		
		setupListViewPager();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_EVENT_POSITION, currentPosition);
		outState.putLong(ARG_EVENT_ID, currentEventId);
		outState.putInt(ARG_SEVERITY, currentSeverity);
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
		currentPosition = position;
	}

}
