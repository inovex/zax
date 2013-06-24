package com.inovex.zabbixmobile.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Host;
import com.inovex.zabbixmobile.model.Trigger;

public class EventsListAdapter extends BaseServiceAdapter<Event> {

	private static final String TAG = EventsListAdapter.class.getSimpleName();
	private int mTextViewResourceId = R.layout.events_list_item;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public EventsListAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(mTextViewResourceId, parent, false);

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
		
		String hostNames = e.getHostNames();
		if(hostNames == null) {
			title.setText("");
			Log.w(TAG, "No host defined for Event with ID "
					+ e.getId());
		} else
			title.setText(hostNames);
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
	public long getItemId(int position) {
		return getItem(position).getId();
	}

}
