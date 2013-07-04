package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.ChecksApplicationsPagerAdapter;
import com.inovex.zabbixmobile.model.Host;
import com.viewpagerindicator.TitlePageIndicator;

public class ChecksDetailsFragment extends BaseServiceConnectedFragment {

	public static final String TAG = ChecksDetailsFragment.class
			.getSimpleName();

	private int mHostPosition = 0;
	private long mHostId;
	private static final String ARG_HOST_POSITION = "arg_host_position";
	private static final String ARG_HOST_ID = "arg_host_id";

	private TextView titleView;

	protected ViewPager mDetailsPager;
	protected TitlePageIndicator mDetailsPageIndicator;
	protected ChecksApplicationsPagerAdapter mDetailsPagerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mHostPosition = savedInstanceState.getInt(ARG_HOST_POSITION, 0);
			mHostId = savedInstanceState.getLong(ARG_HOST_ID, 0);
		}
		setRetainInstance(true);
		Log.d(TAG, "Host position: " + mHostPosition + "; id: " + mHostId);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_HOST_POSITION, mHostPosition);
		outState.putLong(ARG_HOST_ID, mHostId);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_checks_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		titleView = (TextView) view.findViewById(R.id.checks_title);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		setupDetailsViewPager();

	}

	public void selectHost(int position, long id) {
		this.mHostPosition = position;
		this.mHostId = id;
		Host h = mZabbixDataService.getHostById(mHostId);
		// update view pager
		titleView.setText("Host: " + h.getName());
		mZabbixDataService.loadApplicationsByHostId(mHostId, this);
		mDetailsPageIndicator.setCurrentItem(0);
	}

	/**
	 * Causes a redraw of the page indicator. This needs to be called when the
	 * adapter's contents are updated.
	 */
	public void redrawPageIndicator() {
		mDetailsPageIndicator.invalidate();
		// this is necessary to refresh the adapter
		mDetailsPageIndicator.onPageSelected(0);
	}

	/**
	 * Performs the setup of the view pager used to swipe between details pages.
	 */
	protected void setupDetailsViewPager() {
		Log.d(TAG, "setupViewPager");

		retrievePagerAdapter();
		if (mDetailsPagerAdapter != null) {
			mDetailsPagerAdapter.setFragmentManager(getChildFragmentManager());

			// initialize the view pager
			mDetailsPager = (ViewPager) getView().findViewById(
					R.id.checks_view_pager);
			mDetailsPager.setAdapter(mDetailsPagerAdapter);

			// Initialize the page indicator
			mDetailsPageIndicator = (TitlePageIndicator) getView()
					.findViewById(R.id.checks_page_indicator);
			mDetailsPageIndicator.setViewPager(mDetailsPager);
			mDetailsPageIndicator.setCurrentItem(mHostPosition);
			mDetailsPageIndicator
					.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

						@Override
						public void onPageScrollStateChanged(int arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onPageScrolled(int arg0, float arg1,
								int arg2) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onPageSelected(int position) {
							Log.d(TAG, "detail page selected: " + position);

							mDetailsPagerAdapter.setCurrentPosition(position);
							mZabbixDataService
									.loadItemsByApplicationId(mDetailsPagerAdapter
											.getCurrentItem().getId());

							// propagate page change only if there actually was
							// a
							// change -> prevent infinite propagation
							// mDetailsPagerAdapter.setCurrentPosition(position);
							// if (position != mPosition)
							// mCallbackMain.onListItemSelected(position,
							// mDetailsPagerAdapter.getItemId(position));
						}
					});
		}
	}

	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getChecksApplicationsPagerAdapter();
	}

}
