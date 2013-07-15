package com.inovex.zabbixmobile.activities.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import android.app.Activity;
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
import com.inovex.zabbixmobile.model.HistoryDetail;
import com.inovex.zabbixmobile.model.Item;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class ChecksItemsDetailsPage extends BaseDetailsPage {

	private Item mItem;
	private String mTitle = "";
	private CharSequence historyDetailsString;
	private Collection<HistoryDetail> mHistoryDetails;
	private boolean mHistoryDetailsImported = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.page_item_details, null);
		// if (savedInstanceState != null)
		// mEventId = savedInstanceState.getLong(ARG_EVENT_ID, -1);

		return rootView;
	}

	@Override
	protected void fillDetailsText() {
		StringBuilder sb = new StringBuilder();
		if (mItem != null) {
			sb.append("Item: \n\n");
			sb.append("ID: " + mItem.getId() + "\n");
			sb.append("description: " + mItem.getDescription() + "\n");
			sb.append("last value: " + mItem.getLastValue() + mItem.getUnits()
					+ "\n");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mItem.getLastClock());
			java.text.DateFormat dateFormatter = SimpleDateFormat
					.getDateTimeInstance(SimpleDateFormat.SHORT,
							SimpleDateFormat.SHORT, Locale.getDefault());
			sb.append("last clock: "
					+ String.valueOf(dateFormatter.format(cal.getTime()))
					+ "\n");
			TextView text = (TextView) getView()
					.findViewById(R.id.item_details);
			text.setText(sb.toString());
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		if (!mHistoryDetailsImported && mItem != null)
			mZabbixDataService.loadHistoryDetailsByItemId(mItem.getId(), this);
	}

	/**
	 * Sets the item for this page. This also triggers an import of history
	 * details for displaying the graph.
	 * 
	 * @param item
	 */
	public void setItem(Item item) {
		this.mItem = item;
		if (!mHistoryDetailsImported && mZabbixDataService != null)
			mZabbixDataService.loadHistoryDetailsByItemId(mItem.getId(), this);
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

	protected void showGraph() {
		showGraph(mItem);
	}

}
