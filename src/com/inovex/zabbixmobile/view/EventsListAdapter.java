package com.inovex.zabbixmobile.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsListAdapter extends BaseAdapter {

	private static final String TAG = EventsListAdapter.class.getSimpleName();
	private int mTextViewResourceId;
	private ArrayList<Event> mObjects;
	private ZabbixDataService mZabbixDataService;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public EventsListAdapter(ZabbixDataService service, int textViewResourceId) {
		super();
		this.mTextViewResourceId = textViewResourceId;
		this.mObjects = new ArrayList<Event>();
		this.mZabbixDataService = service;
	}

	/**
	 * Constructor with an initial collection of events.
	 * 
	 * @param inflater
	 * @param textViewResourceId
	 * @param objects
	 */
	public EventsListAdapter(ZabbixDataService service, int textViewResourceId,
			Collection<Event> objects) {
		this(service, textViewResourceId);
		this.mObjects.addAll(objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = mZabbixDataService.getInflater().inflate(mTextViewResourceId, parent, false);

		}

		TextView title = (TextView) row.findViewById(R.id.events_entry_host);
		TextView description = (TextView) row
				.findViewById(R.id.events_entry_description);
		TextView clock = (TextView) row.findViewById(R.id.events_entry_clock);

		Event e = getItem(position);
		Trigger t = e.getTrigger();
		if (t == null) {
			description.setText("no trigger defined.");
			Log.w(TAG, "No trigger defined for Event with ID "
					+ e.getId());
		} else 
			description.setText(String.valueOf(t.getDescription()));
		
		Host h = e.getHost();
		if(t == null) {
			title.setText("");
			Log.w(TAG, "No host defined for Event with ID "
					+ e.getId());
		} else
			title.setText(h.getHost());
		title.append(String.valueOf("[id: " + e.getId() + "]"));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(e.getClock());
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		clock.setText(String.valueOf(dateFormatter.format(cal.getTime())));

		return row;
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public Event getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mObjects.get(position).getId();
	}

	public void addAll(Collection<? extends Event> collection) {
		mObjects.addAll(collection);
	}
	
	public void clear() {
		mObjects.clear();
	}

}
