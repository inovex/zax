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
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsListAdapter extends BaseAdapter {

	private Context mContext;
	private int mTextViewResourceId;
	private ArrayList<Event> mObjects;
	private LayoutInflater mInflater;

	/**
	 * Constructor.
	 * 
	 * @param context
	 * @param textViewResourceId
	 */
	public EventsListAdapter(Context context, int textViewResourceId) {
		super();
		this.mTextViewResourceId = textViewResourceId;
		this.mObjects = new ArrayList<Event>();
		this.mContext = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Constructor with an initial collection of events.
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public EventsListAdapter(Context context, int textViewResourceId,
			Collection<Event> objects) {
		this(context, textViewResourceId);
		this.mObjects.addAll(objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = mInflater.inflate(mTextViewResourceId, parent, false);

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

	/**
	 * Adds a collection of events to this adapter.
	 * 
	 * @param collection
	 *            the collection to be added
	 */
	public void addAll(Collection<? extends Event> collection) {
		mObjects.addAll(collection);
	}

}
