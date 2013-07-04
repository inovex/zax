package com.inovex.zabbixmobile.activities.fragments;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.ChecksItemsPagerAdapter;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class ChecksItemsDetailsFragment extends BaseServiceConnectedFragment {

	public static final String TAG = ChecksItemsDetailsFragment.class
			.getSimpleName();

	private int mPosition = 0;
	private long mItemId;

	protected ViewPager mDetailsPager;
	protected TitlePageIndicator mDetailsPageIndicator;
	protected ChecksItemsPagerAdapter mDetailsPagerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_items_details, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);

		setupDetailsViewPager();
	}

	public void selectItem(int position, long id) {
		this.mPosition = position;
		this.mItemId = id;
		// update view pager
		mDetailsPageIndicator.setCurrentItem(position);
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
					R.id.items_view_pager);
			mDetailsPager.setAdapter(mDetailsPagerAdapter);

			// Initialize the page indicator
			mDetailsPageIndicator = (TitlePageIndicator) getView()
					.findViewById(R.id.items_page_indicator);
			mDetailsPageIndicator.setViewPager(mDetailsPager);
			mDetailsPageIndicator.setCurrentItem(mPosition);
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
//							mZabbixDataService
//									.loadItemsByApplicationId(mDetailsPagerAdapter
//											.getCurrentItem().getId());

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
				.getChecksItemsPagerAdapter();
	}

}
