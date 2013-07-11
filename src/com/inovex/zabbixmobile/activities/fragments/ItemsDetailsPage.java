package com.inovex.zabbixmobile.activities.fragments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.adapters.EventsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Item;

/**
 * Represents one page of the event details view pager (see
 * {@link EventsDetailsPagerAdapter} ). Shows the details of a specific event.
 * 
 */
public class ItemsDetailsPage extends BaseServiceConnectedFragment {

	private Item mItem;
	private String mTitle = "";

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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO: on orientation change, mEvent is not set ->
		// NullPointerException
		StringBuilder sb = new StringBuilder();
		sb.append("Item: \n\n");
		sb.append("ID: " + mItem.getId() + "\n");
		sb.append("description: " + mItem.getDescription() + "\n");
		sb.append("last value: " + mItem.getLastValue() + mItem.getUnits()
				+ "\n");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mItem.getLastClock());
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		sb.append("last clock: "
				+ String.valueOf(dateFormatter.format(cal.getTime())) + "\n");
		TextView text = (TextView) getView().findViewById(R.id.checks_title);
		text.setText(sb.toString());
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		super.onServiceConnected(name, service);
		mZabbixDataService.loadHistoryDetailsByItemId(mItem.getId());
	}

	public void setItem(Item item) {
		this.mItem = item;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getTitle() {
		return mTitle;
	}

}
