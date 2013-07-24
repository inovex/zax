package com.inovex.zabbixmobile.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Adapter for the events list (see {@link EventsListPage}).
 * 
 */
public class EventsListAdapter extends BaseServiceAdapter<Event> {

	private static final String TAG = EventsListAdapter.class.getSimpleName();
	private int mTextViewResourceId = R.layout.list_item_severity;

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

		ImageView statusImage = (ImageView) row
				.findViewById(R.id.severity_list_item_status);

		TextView title = (TextView) row
				.findViewById(R.id.severity_list_item_host);
		TextView description = (TextView) row
				.findViewById(R.id.severity_list_item_description);
		TextView clock = (TextView) row
				.findViewById(R.id.severity_list_item_clock);

		Event e = getItem(position);
		Trigger t = e.getTrigger();

		String hostNames = e.getHostNames();
		if (hostNames == null) {
			hostNames = "";
			Log.w(TAG, "No host defined for Event with ID " + e.getId());
		}
		title.setText(hostNames);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(e.getClock());
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		clock.setText(String.valueOf(dateFormatter.format(cal.getTime())));

		if (e.getValue() == Event.VALUE_OK)
			statusImage.setImageResource(R.drawable.ok);
		else
			statusImage.setImageResource(R.drawable.problem);
		if (t == null) {
			Log.w(TAG, "No trigger defined for Event with ID " + e.getId());
		} else
			description.setText(String.valueOf(t.getDescription()));

		return row;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

}
