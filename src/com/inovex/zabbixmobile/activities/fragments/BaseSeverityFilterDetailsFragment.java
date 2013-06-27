package com.inovex.zabbixmobile.activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseSeverityPagerAdapter;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.data.ZabbixDataService.ZabbixDataBinder;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.viewpagerindicator.CirclePageIndicator;

public abstract class BaseSeverityFilterDetailsFragment<T> extends SherlockFragment
		implements ServiceConnection {

	public static final String TAG = BaseSeverityFilterDetailsFragment.class
			.getSimpleName();
	protected ViewPager mDetailsPager;
	protected int mPosition = 0;
	protected long mCurrentItemId = 0;
	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	protected CirclePageIndicator mDetailsCircleIndicator;
	protected ZabbixDataService mZabbixDataService;
	private OnSeverityListItemSelectedListener mCallbackMain;
	protected BaseSeverityPagerAdapter<T> mDetailsPagerAdapter;

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		// we need to do this after the view was created!!
		Intent intent = new Intent(getSherlockActivity(),
				ZabbixDataService.class);
		getSherlockActivity().bindService(intent, this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		getSherlockActivity().unbindService(this);
	}

	/**
	 * Selects an event which shall be displayed in the view pager.
	 * 
	 * @param position
	 *            list position
	 * @param severity
	 *            severity (this is used to retrieve the correct pager adapter
	 * @param id
	 *            item identifier
	 */
	public void selectItem(int position, TriggerSeverity severity, long id) {
		Log.d(TAG, "selectItem(" + position + ")");
		setSeverity(severity);
		setPosition(position);
		setCurrentItemId(id);
	}

	public void setPosition(int position) {
		this.mPosition = position;
		if (mDetailsCircleIndicator != null) {
			mDetailsCircleIndicator.setCurrentItem(position);
		}
	}

	public void setCurrentItemId(long itemId) {
		this.mCurrentItemId = itemId;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "onServiceConnected");
		ZabbixDataBinder binder = (ZabbixDataBinder) service;
		mZabbixDataService = binder.getService();

		setupDetailsViewPager();

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mZabbixDataService = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Log.d(TAG, "onCreate");
		// setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_severity_details,
				container, false);
		Log.d(TAG, "onCreateView");
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	
		Log.d(TAG, "onViewCreated");
		// if (savedInstanceState != null) {
		// mPosition = savedInstanceState.getInt(ARG_EVENT_POSITION, 0);
		// mEventId = savedInstanceState.getLong(ARG_EVENT_ID, 0);
		// mSeverity = savedInstanceState.getInt(ARG_SEVERITY,
		// TriggerSeverities.ALL.getNumber());
		// }
	
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// outState.putInt(ARG_EVENT_POSITION, mPosition);
		// outState.putLong(ARG_EVENT_ID, mEventId);
		// outState.putInt(ARG_SEVERITY, mSeverity);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
	
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnSeverityListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnEventSelectedListener.");
		}
	}

	/**
	 * 
	 */
	protected abstract void retrievePagerAdapter();

	/**
	 * Performs the setup of the view pager used to swipe between details pages.
	 */
	protected void setupDetailsViewPager() {
		Log.d(TAG, "setupViewPager");
	
		retrievePagerAdapter();
		mDetailsPagerAdapter.setFragmentManager(getChildFragmentManager());
	
		// initialize the view pager
		mDetailsPager = (ViewPager) getView().findViewById(
				R.id.severity_view_pager);
		mDetailsPager.setAdapter(mDetailsPagerAdapter);
	
		// Initialize the circle indicator
		mDetailsCircleIndicator = (CirclePageIndicator) getView().findViewById(
				R.id.severity_circle_page_indicator);
		mDetailsCircleIndicator.setViewPager(mDetailsPager);
		mDetailsCircleIndicator.setCurrentItem(mPosition);
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
	
						// mCallbackMain.onEventSelected(position);
					}
	
				});
	
	}

	/**
	 * Sets the current severity and updates the pager adapter.
	 * 
	 * @param severity
	 *            current severity
	 */
	protected void setSeverity(TriggerSeverity severity) {
		this.mSeverity = severity;
		retrievePagerAdapter();
		// the adapter could be fresh -> set fragment manager
		mDetailsPagerAdapter.setFragmentManager(getChildFragmentManager());
		mDetailsPager.setAdapter(mDetailsPagerAdapter);
	}

}
