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

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksHostsFragment;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Host;

/**
 * Adapter for the list of hosts used by {@link ChecksHostsFragment}.
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
		Host item = getItem(position);
		if (item != null)
			return item.getId();
		return 0;
	}

}
