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

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.BaseSeverityPagerAdapter;
import com.inovex.zabbixmobile.listeners.OnListItemSelectedListener;
import com.inovex.zabbixmobile.model.TriggerSeverity;
import com.viewpagerindicator.TitlePageIndicator;

public abstract class BaseSeverityFilterDetailsFragment<T> extends
		BaseServiceConnectedFragment {

	public static final String TAG = BaseSeverityFilterDetailsFragment.class
			.getSimpleName();

	private static final String ARG_ITEM_POSITION = "arg_item_position";
	private static final String ARG_ITEM_ID = "arg_item_id";
	private static final String ARG_SEVERITY = "arg_severity";
	protected ViewPager mDetailsPager;
	protected int mPosition = 0;
	protected long mItemId = 0;
	protected TriggerSeverity mSeverity = TriggerSeverity.ALL;
	protected TitlePageIndicator mDetailsPageIndicator;
	private OnListItemSelectedListener mCallbackMain;
	protected BaseSeverityPagerAdapter<T> mDetailsPagerAdapter;

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
	public void selectItem(int position, long id) {
		Log.d(TAG, "selectItem(" + position + ")");
		setPosition(position);
		setCurrentItemId(id);
	}

	public void setPosition(int position) {
		this.mPosition = position;
		if (mDetailsPageIndicator != null) {
			mDetailsPageIndicator.setCurrentItem(position);
		}
	}

	public void setCurrentItemId(long itemId) {
		this.mItemId = itemId;
	}

	/**
	 * Sets the current severity and updates the pager adapter.
	 * 
	 * @param severity
	 *            current severity
	 */
	public void setSeverity(TriggerSeverity severity) {
		// exchange adapter if it's necessary
		// if(severity == this.mSeverity)
		// return;
		this.mSeverity = severity;
		retrievePagerAdapter();
		// the adapter could be fresh -> set fragment manager
		mDetailsPagerAdapter.setFragmentManager(getChildFragmentManager());
		mDetailsPager.setAdapter(mDetailsPagerAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Log.d(TAG, "onCreate");
		// setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_severity_details,
				container, false);
		Log.d(TAG, "onCreateView");
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.d(TAG, "onViewCreated");
		if (savedInstanceState != null) {
			mPosition = savedInstanceState.getInt(ARG_ITEM_POSITION, 0);
			mItemId = savedInstanceState.getLong(ARG_ITEM_ID, 0);
			mSeverity = TriggerSeverity.getSeverityByNumber(savedInstanceState
					.getInt(ARG_SEVERITY, TriggerSeverity.ALL.getNumber()));
		}

	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		setupDetailsViewPager();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_ITEM_POSITION, mPosition);
		outState.putLong(ARG_ITEM_ID, mItemId);
		outState.putInt(ARG_SEVERITY, mSeverity.getNumber());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallbackMain = (OnListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnListItemSelectedListener.");
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

		// Initialize the page indicator
		mDetailsPageIndicator = (TitlePageIndicator) getView().findViewById(
				R.id.severity_page_indicator);
		mDetailsPageIndicator.setViewPager(mDetailsPager);
		mDetailsPageIndicator.setCurrentItem(mPosition);
		mDetailsPageIndicator
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

						// propagate page change only if there actually was a
						// change -> prevent infinite propagation
						mDetailsPagerAdapter.setCurrentPosition(position);
						if (position != mPosition)
							mCallbackMain.onListItemSelected(position,
									mDetailsPagerAdapter.getItemId(position));
					}
				});
	}

}
