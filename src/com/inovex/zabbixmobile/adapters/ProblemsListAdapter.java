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

		ImageView statusImage = (ImageView) row
				.findViewById(R.id.severity_list_item_status);

		TextView title = (TextView) row
				.findViewById(R.id.severity_list_item_host);
		TextView description = (TextView) row
				.findViewById(R.id.severity_list_item_description);
		TextView clock = (TextView) row
				.findViewById(R.id.severity_list_item_clock);

		Trigger t = getItem(position);

		String hostNames = t.getHostNames();
		if (hostNames == null) {
			hostNames = "";
			Log.w(TAG, "No host defined for trigger with ID " + t.getId());
		}
		String desc = String.valueOf(t.getDescription());
		if (hostNames.length() > 0)
			desc = desc.replaceAll("\\{HOSTNAME\\}", hostNames);
		description.setText(desc);
		title.setText(hostNames);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t.getLastChange());
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		clock.setText(String.valueOf(dateFormatter.format(cal.getTime())));

		switch (t.getPriority()) {
		case DISASTER:
		case HIGH:
			statusImage.setImageResource(R.drawable.severity_high);
			break;
		case INFORMATION:
			statusImage.setImageResource(R.drawable.ok);
			break;
		default:
			statusImage.setImageResource(R.drawable.severity_average);
		}

		return row;
	}

	@Override
	public int getItemViewType(int position) {
		Trigger t = getItem(position);
		switch (t.getPriority()) {
		// TODO: change when images are actually different for severities
		case DISASTER:
		case HIGH:
			return 0;
		case INFORMATION:
			return 1;
		default:
			return 2;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public long getItemId(int position) {
		Trigger item = getItem(position);
		if (item != null)
			return item.getId();
		return 0;
	}

}
