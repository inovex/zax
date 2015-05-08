/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

		ViewHolder viewHolder;

		if (row == null) {
			row = getInflater().inflate(android.R.layout.simple_list_item_1,
					parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) row.findViewById(android.R.id.text1);
			row.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) row.getTag();
		}

		ZabbixServer server = getItem(position);

		viewHolder.name.setText(server.getName());

		return row;
	}

	static class ViewHolder {
		TextView name;
	}

}
