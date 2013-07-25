package com.inovex.zabbixmobile.activities.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Item;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link EventsDetailsPagerAdapter}).
 * 
 */
public class ChecksItemsFragment extends BaseDetailsPage {

	public static final String TAG = ChecksItemsFragment.class.getSimpleName();

	private Item mItem;

	private boolean mLoadingSpinnerVisible = false;

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

		if (mLoadingSpinnerVisible)
			showLoadingSpinner();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		if (mItem != null)
			mZabbixDataService.loadHistoryDetailsByItem(mItem, true, this);
	}

	/**
	 * Sets the item for this page. This also triggers an import of history
	 * details for displaying the graph.
	 * 
	 * @param item
	 */
	public void setItem(Item item) {
		this.mItem = item;
		if (mZabbixDataService != null && item != null) {
			fillDetailsText();
			showGraphLoadingSpinner();
			mZabbixDataService.loadHistoryDetailsByItem(mItem, true, this);
		}
	}

	/**
	 * Shows a loading spinner instead of the item details.
	 */
	public void showLoadingSpinner() {
		mLoadingSpinnerVisible = true;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.progress_layout);
			if (progressLayout != null)
				progressLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Dismisses the loading spinner view.
	 * 
	 * If the view has not yet been created, the status is saved and when the
	 * view is created, the spinner will not be shown at all.
	 */
	public void dismissLoadingSpinner() {
		mLoadingSpinnerVisible = false;
		if (getView() != null) {
			LinearLayout progressLayout = (LinearLayout) getView()
					.findViewById(R.id.progress_layout);
			if (progressLayout != null) {
				progressLayout.setVisibility(View.GONE);
			}
		}

	}

	protected void showGraph() {
		showGraph(mItem);
	}

	@Override
	protected void fillDetailsText() {
		if (mItem != null) {
			((TextView) getView().findViewById(R.id.item_details_name))
					.setText(mItem.getDescription());

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mItem.getLastClock());
			java.text.DateFormat dateFormatter = SimpleDateFormat
					.getDateTimeInstance(SimpleDateFormat.SHORT,
							SimpleDateFormat.SHORT, Locale.getDefault());
			((TextView) getView().findViewById(R.id.latest_data)).setText(mItem
					.getLastValue()
					+ mItem.getUnits()
					+ " "
					+ getResources().getString(R.string.at)
					+ " "
					+ dateFormatter.format(cal.getTime()));
		}
	}

}
