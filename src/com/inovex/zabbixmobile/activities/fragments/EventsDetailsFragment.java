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
import com.inovex.zabbixmobile.activities.fragments.EventsListFragment.Severities;
import com.inovex.zabbixmobile.model.DataAccess;
import com.inovex.zabbixmobile.model.Event;
import com.viewpagerindicator.CirclePageIndicator;

public class EventsDetailsFragment extends SherlockFragment {

	public static final String TAG = EventsDetailsFragment.class
			.getSimpleName();
	public static final String ARG_EVENT_POSITION = "event_position";
	public static final String ARG_EVENT_ID = "event_id";
	public static final String ARG_SEVERITY = "severity";

	ViewPager mDetailsPager;
	EventsDetailsPagerAdapter mCurrentDetailsPagerAdapter;
	SparseArray<EventsDetailsPagerAdapter> mEventDetailsPagerAdapters = new SparseArray<EventsDetailsPagerAdapter>();

	private OnEventSelectedListener mCallbackMain;
	private int position = 0;
	private long eventId = 0;
	private int severity = Severities.ALL.getNumber();
	private CirclePageIndicator mDetailsCircleIndicator;

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

		if (savedInstanceState != null) {
			position = savedInstanceState.getInt(ARG_EVENT_POSITION, 0);
			eventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
			severity = savedInstanceState.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
		}

		Bundle args = getArguments();
		if (args != null) {
			position = args.getInt(ARG_EVENT_POSITION, 0);
			eventId = args.getLong(ARG_EVENT_ID, 0);
			severity = args.getInt(ARG_SEVERITY, Severities.ALL.getNumber());
		}
		
		setupDetailsViewPager();
		
		mDetailsCircleIndicator.setCurrentItem(position);

		// selectCategory(severity, eventNumber);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_EVENT_POSITION, position);
		outState.putLong(ARG_EVENT_ID, eventId);
		outState.putInt(ARG_SEVERITY, severity);
		super.onSaveInstanceState(outState);
	}

	private void setupDetailsViewPager() {
		mDetailsPager = (ViewPager) getView().findViewById(
				R.id.details_view_pager);

		mCurrentDetailsPagerAdapter = new EventsDetailsPagerAdapter(
				getChildFragmentManager());

		mDetailsPager.setAdapter(mCurrentDetailsPagerAdapter);

		// Bind the title indicator to the adapter
		mDetailsCircleIndicator = (CirclePageIndicator) getView().findViewById(
				R.id.details_circle_page_indicator);
		mDetailsCircleIndicator.setViewPager(mDetailsPager);

		mDetailsCircleIndicator
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageScrollStateChanged(int arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onPageSelected(int position) {
						Log.d(TAG, "detail page selected: " + position);

						mCallbackMain.onEventSelected(position);
					}

				});

	}

	// TODO: replace; possibly obsolete...
	private void selectCategory(int category, int selectedEvent) {
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
				mCurrentDetailsPagerAdapter = new EventsDetailsPagerAdapter(
						getChildFragmentManager());
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
		if (mDetailsCircleIndicator != null) {
			// mDetailsPager.setCurrentItem(position);
			mDetailsCircleIndicator.setCurrentItem(position);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// inflater.inflate(R.menu.fragment_events_details, menu);
	}

	class EventsDetailsPagerAdapter extends FragmentStatePagerAdapter implements
			OnEventSelectedListener {

		private List<Event> events;
		private ArrayList<EventsDetailsPage> fragments = new ArrayList<EventsDetailsPage>();

		public EventsDetailsPagerAdapter(FragmentManager fm) {
			super(fm);
			Log.d(TAG, "creating DetailsPagerAdapter for severity " + severity);

			fragments.clear();
			DataAccess dataAccess = DataAccess
					.getInstance(getSherlockActivity());
			try {
				events = dataAccess.getEventsBySeverity(severity);
				EventsDetailsPage f;
				Event event;
				for (int i = 0; i < events.size(); i++) {
					Log.d(TAG, "Creating page for event " + i + ": " + events.toString());
					f = new EventsDetailsPage();
					event = events.get(i);
					Bundle args = new Bundle();
					args.putLong(EventsDetailsPage.ARG_EVENT_ID, event.getId());
					f.setArguments(args);
					fragments.add(f);
				}
				notifyDataSetChanged();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return fragments.size();
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
