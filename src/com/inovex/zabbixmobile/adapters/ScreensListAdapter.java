package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksHostsFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Screen;

/**
 * Adapter for the list of hosts used by {@link ChecksHostsFragment}.
 * 
 */
public class ScreensListAdapter extends BaseServiceAdapter<Screen> {

	private static final String TAG = ScreensListAdapter.class.getSimpleName();
	private int mTextViewResourceId = R.layout.list_item_screens;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public ScreensListAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(mTextViewResourceId, parent, false);

		}

		TextView title = (TextView) row.findViewById(R.id.screen_entry_name);

		Screen s = getItem(position);

		title.setText(s.getName());

		return row;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

}
