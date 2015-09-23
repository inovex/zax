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

		StringBuilder titleBuilder = new StringBuilder();
		if(s.getHost() != null) {
			titleBuilder.append(s.getHost().getName() + " - ");
		}
		titleBuilder.append(s.getName());
		title.setText(titleBuilder.toString());

		return row;
	}

	@Override
	public long getItemId(int position) {
		Screen item = getItem(position); 
		if (item != null)
			return item.getScreenId();
		return 0;
	}

}
