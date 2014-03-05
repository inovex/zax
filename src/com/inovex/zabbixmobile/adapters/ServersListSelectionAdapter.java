package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.ZabbixServer;

public class ServersListSelectionAdapter extends BaseServiceAdapter<ZabbixServer> {

	public ServersListSelectionAdapter(ZabbixDataService service) {
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
			row = getInflater().inflate(R.layout.list_item_servers_selection, parent, false);
		}
		
		ZabbixServer server = getItem(position);
		
		TextView name = (TextView)row.findViewById(R.id.server_name);
		name.setText(server.getName());
		
		row.setTag(position);
		return row;
	}

}
