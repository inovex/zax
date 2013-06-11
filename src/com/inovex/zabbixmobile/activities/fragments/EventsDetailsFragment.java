package com.inovex.zabbixmobile.activities.fragments;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.DataAccess;
import com.inovex.zabbixmobile.model.Event;
import com.viewpagerindicator.CirclePageIndicator;

public class EventsDetailsFragment extends SherlockFragment {

	public static final String TAG = EventsDetailsFragment.class
			.getSimpleName();
	public static final String ARG_EVENT_ID = "event_id";
	public static final String ARG_SEVERITY = "severity";

	ViewPager mDetailsPager;
	EventDetailsPagerAdapter mCurrentDetailsPagerAdapter;
	SparseArray<EventDetailsPagerAdapter> mEventDetailsPagerAdapters = new SparseArray<EventDetailsPagerAdapter>();

	private OnEventSelectedListener mCallbackMain;
	private int severity = 0;
	private long eventId = 0;
	private CirclePageIndicator mDetailsCircleIndicator;
	private TextView textView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnEventSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnEventSelectedListener.");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_events_details,
				container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		textView = (TextView) getView().findViewById(R.id.events_details_text);

		severity = 0;
		eventId = 0;

		if (savedInstanceState != null) {
			severity = savedInstanceState.getInt(ARG_SEVERITY, 0);
			eventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
		}

		Bundle args = getArguments();
		if (args != null) {
			severity = args.getInt(ARG_SEVERITY, 0);
			eventId = args.getLong(ARG_EVENT_ID, 0);
		}
		
		setupDetailsViewPager();

		textView.setText("Event details: " + eventId + " - " + severity);
		// selectCategory(severity, eventNumber);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_SEVERITY, severity);
		outState.putLong(ARG_EVENT_ID, mDetailsPager.getCurrentItem());
		System.out.println();
	}

	private void setupDetailsViewPager() {
		mDetailsPager = (ViewPager) getView().findViewById(
				R.id.details_view_pager);

		mCurrentDetailsPagerAdapter = new EventDetailsPagerAdapter(
				getChildFragmentManager(), severity);

		mDetailsPager.setAdapter(mCurrentDetailsPagerAdapter);

		// Bind the title indicator to the adapter
		mDetailsCircleIndicator = (CirclePageIndicator) getView().findViewById(
				R.id.details_circle_page_indicator);
		mDetailsCircleIndicator.setViewPager(mDetailsPager);

		// mDetailsCircleIndicator
		// .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
		//
		// @Override
		// public void onPageScrollStateChanged(int arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onPageScrolled(int arg0, float arg1, int arg2) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onPageSelected(int position) {
		// Log.d(TAG,
		// "detail page selected: " + position);
		//
		// mCallbackMain.onEventSelected(position);
		// }
		//
		// });

	}

	public void selectCategory(int category, int selectedEvent) {
		Log.d(TAG, "EventDetailsFragment.selectCategory(" + category + ", "
				+ selectedEvent + ")");

		if (getView() != null) {
			mDetailsPager = (ViewPager) getView().findViewById(
					R.id.details_view_pager);
			// view was already created

			if (mEventDetailsPagerAdapters.size() > category
					&& mEventDetailsPagerAdapters.get(category) != null)
				mCurrentDetailsPagerAdapter = mEventDetailsPagerAdapters
						.get(category);
			else {
				mCurrentDetailsPagerAdapter = new EventDetailsPagerAdapter(
						getChildFragmentManager(), category);
				mEventDetailsPagerAdapters.put(category,
						mCurrentDetailsPagerAdapter);
			}

			mDetailsPager.setAdapter(mCurrentDetailsPagerAdapter);

			mDetailsCircleIndicator.setViewPager(mDetailsPager);

			// mDetailsPager.setCurrentItem(selectedEvent);
			mDetailsCircleIndicator.setCurrentItem(selectedEvent);
		} else {
			// view was not yet created -> set arguments to update status upon
			// creation
			severity = category;
			eventId = selectedEvent;
		}

	}

	public void selectEvent(int position) {
		Log.d(TAG, "EventDetailsFragment:selectEvent(" + position + ")");
		if (mDetailsPager != null) {
			// mDetailsPager.setCurrentItem(position);
			mDetailsCircleIndicator.setCurrentItem(position);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// inflater.inflate(R.menu.fragment_events_details, menu);
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// Log.d("MENU", "options item selected: " + item.getItemId() + " - "
	// + R.id.details_acknowledge + ": " + item.getTitle()
	// + " eventNumber: " + mDetailsPager.getCurrentItem());
	// if (item.getItemId() == R.id.details_acknowledge) {
	// EventDetailsPage p = (EventDetailsPage) mCurrentDetailsPagerAdapter
	// .instantiateItem(mDetailsPager,
	// mDetailsPager.getCurrentItem());
	// }
	// return true;
	// }

	class EventDetailsPagerAdapter extends FragmentStatePagerAdapter implements
			OnEventSelectedListener {

		private List<Event> events;
		private int currentItem;
		private ArrayList<EventsDetailsPage> fragments = new ArrayList<EventsDetailsPage>();

		public EventDetailsPagerAdapter(FragmentManager fm, int s) {
			super(fm);
			Log.d(TAG, "creating new DetailsPagerAdapter for severity " + s);

			fragments.clear();
			DataAccess dataAccess = DataAccess
					.getInstance(getSherlockActivity());
			// dataAccess.getEventById(eventNumber);
			try {
				events = dataAccess.getEventsBySeverity(severity);
				Log.d(TAG, "set severity to " + s);
				EventsDetailsPage f;
				Event event;
				for (int i = 0; i < events.size(); i++) {
					Log.d(TAG, "Event " + i + ": " + events.get(i).getDetailedString());
					f = new EventsDetailsPage();
					event = events.get(i);
					f.setEvent(event);
					Bundle args = new Bundle();
					// args.putInt(EventsCategoryPage.ARG_CATEGORY_NUMBER,
					// category);
					// args.putString(EventsCategoryPage.ARG_CATEGORY_NAME,
					// categoryNames[category]);
					f.setArguments(args);
					fragments.add(f);
				}
				notifyDataSetChanged();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// String[] categoryNames = DummyData.categories;

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
			// EventDetailsFragment fragment = new EventDetailsFragment();
			// Event event = null;
			// System.out.println("DEBUG: getting item " + i + " from page " +
			// category);
			// // event = DummyData.events.get(section)[i];
			// event = events[i];
			//
			// fragment.setEvent(event);
			// Bundle args = new Bundle();
			// args.putInt(EventCategoryFragment.ARG_CATEGORY_NUMBER, category);
			// args.putString(EventCategoryFragment.ARG_CATEGORY_NAME,
			// sectionNames[category]);
			// fragment.setArguments(args);
			// return fragment;
		}

		@Override
		public int getCount() {
			return fragments.size();
			// return DummyData.events.get(category).length;
		}

		@Override
		public void onEventClicked(int position) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEventSelected(int position, int severity, long id) {
			// TODO Auto-generated method stub

		}

	}
}
