package com.inovex.zabbixmobile.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsListAdapter extends BaseAdapter {

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
		if (t == null)
			throw new RuntimeException("No trigger defined for Event with ID "
					+ e.getId());
		title.setText(String.valueOf("id: " + e.getId()));
		description.setText(String.valueOf(t.getDescription()));
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		clock.setText(String.valueOf(dateFormatter.format(e.getClock())));

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
