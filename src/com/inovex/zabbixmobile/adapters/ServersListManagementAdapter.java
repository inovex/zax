package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersListManagementAdapter extends
		BaseServiceAdapter<ZabbixServer> {

	public ServersListManagementAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(R.layout.list_item_servers_management,
					parent, false);
		}

		ZabbixServer server = getItem(position);

		TextView name = (TextView) row.findViewById(R.id.server_name);
		name.setText(server.getName());

		TextView url = (TextView) row.findViewById(R.id.server_url);
		url.setText(server.getUrl());

		row.setTag(position);
		return row;
	}

}
