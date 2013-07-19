package com.inovex.zabbixmobile.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Adapter for the problems list (see {@link ProblemsListPage}).
 * 
 */
public class ProblemsListAdapter extends BaseServiceAdapter<Trigger> {

	private static final String TAG = ProblemsListAdapter.class.getSimpleName();
	private int mTextViewResourceId = R.layout.list_item_severity;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public ProblemsListAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(mTextViewResourceId, parent, false);

		}

		TextView title = (TextView) row
				.findViewById(R.id.severity_list_item_host);
		TextView description = (TextView) row
				.findViewById(R.id.severity_list_item_description);
		TextView clock = (TextView) row
				.findViewById(R.id.severity_list_item_clock);

		Trigger t = getItem(position);
		description.setText(String.valueOf(t.getDescription()));

		String hostNames = t.getHostNames();
		if (hostNames == null) {
			hostNames = "";
			Log.w(TAG, "No host defined for Event with ID " + t.getId());
		}
		title.setText(hostNames);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t.getLastChange());
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
