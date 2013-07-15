package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksListFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Host;

/**
 * Adapter for the list of hosts used by {@link ChecksListFragment}.
 * 
 */
public class HostsListAdapter extends BaseServiceAdapter<Host> {

	private static final String TAG = HostsListAdapter.class.getSimpleName();
	private int mTextViewResourceId = R.layout.list_item_hosts;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public HostsListAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(mTextViewResourceId, parent, false);

		}

		TextView title = (TextView) row.findViewById(R.id.host_entry_name);

		Host h = getItem(position);

		title.setText(h.getName());

		return row;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

}
