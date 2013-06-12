package com.inovex.zabbixmobile.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsArrayAdapter extends ArrayAdapter<Event> {

	private Context context;
	private int textViewResourceId;
	protected List<Event> mObjects;

	public EventsArrayAdapter(Context context, int textViewResourceId,
			List<Event> objects) {
		super(context, textViewResourceId, objects);
		this.textViewResourceId = textViewResourceId;
		this.context = context;
		this.mObjects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(textViewResourceId, parent, false);

		}

		TextView title = (TextView) row.findViewById(R.id.events_entry_host);
		TextView description = (TextView) row
				.findViewById(R.id.events_entry_description);
		TextView clock = (TextView) row.findViewById(R.id.events_entry_clock);

		Event e = getItem(position);
		Trigger t = e.getTrigger();
		if(t == null)
			throw new RuntimeException("No trigger defined for Event with ID " + e.getId());
		title.setText(String.valueOf("id: " + e.getId() + " status: " + t.getStatus() + " severity: " + t.getPriority()));
		description.setText(String.valueOf(t.getDescription()));
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());
		clock.setText(String.valueOf(dateFormatter.format(e.getClock())));

		return row;
	}

	@Override
	public long getItemId(int position) {
		return mObjects.get(position).getId();
	}

	@Override
	public Filter getFilter() {
		Filter f = new SeverityFilter();
		return f;
	}

	private class SeverityFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			List<Event> mOriginalValues = mObjects;
			// if (mOriginalValues == null) {
			// synchronized (mObjects) {
			// mOriginalValues = new ArrayList<Event>(mObjects);
			// }
			// }

			if (prefix == null || prefix.length() == 0) {
				List<Event> list;
				synchronized (mObjects) {
					list = new ArrayList<Event>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<Event> values;
				synchronized (mObjects) {
					values = new ArrayList<Event>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<Event> newValues = new ArrayList<Event>();

				for (int i = 0; i < count; i++) {
					final Event value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (String.valueOf(value.getValue()).startsWith(
							prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with
						// space(s)
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mObjects = (List<Event>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
