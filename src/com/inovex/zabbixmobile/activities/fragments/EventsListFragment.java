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
	
	protected static final String TAG = EventsListFragment.class.getSimpleName();
	ViewPager mEventCategoryPager;
	EventCategoryPagerAdapter mEventCategoryPagerAdapter;
	TabPageIndicator mEventCategoryTabIndicator;

	class EventCategoryPagerAdapter extends FragmentStatePagerAdapter {

		String[] categoryNames = new String[] {"all", "disaster", "high", "warning", "information", "not classified"};
		ArrayList<EventsListPage> fragments = new ArrayList<EventsListPage>();

		public EventCategoryPagerAdapter(FragmentManager fm) {
			super(fm);
			EventsListPage f;
			for (int i = 0; i < categoryNames.length; i++) {
				f = new EventsListPage();
				Bundle args = new Bundle();
				args.putInt(EventsListPage.ARG_CATEGORY_NUMBER, i);
				args.putString(EventsListPage.ARG_CATEGORY_NAME,
						categoryNames[i]);
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
			return categoryNames.length;
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
		
		setupCategoryViewPager();
	}
	
	private void setupCategoryViewPager() {
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mEventCategoryPagerAdapter = new EventCategoryPagerAdapter(
				getChildFragmentManager());
		mEventCategoryPager = (ViewPager) getView().findViewById(
				R.id.events_list_viewpager);
		mEventCategoryPager.setAdapter(mEventCategoryPagerAdapter);
		mEventCategoryPager.setOffscreenPageLimit(10);

		// Bind the tab indicator to the adapter
		mEventCategoryTabIndicator = (TabPageIndicator) getView().findViewById(
				R.id.events_list_tabindicator);
		mEventCategoryTabIndicator.setViewPager(mEventCategoryPager);

		mEventCategoryTabIndicator
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						Log.d(TAG,
								"EventCategoryFragment:onPageSelected("
										+ position + ")");
						EventsListPage p = (EventsListPage) mEventCategoryPagerAdapter
								.instantiateItem(mEventCategoryPager,
										mEventCategoryPager.getCurrentItem());
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

}
