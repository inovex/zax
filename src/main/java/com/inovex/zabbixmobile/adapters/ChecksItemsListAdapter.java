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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.ChecksApplicationsPage;
import com.inovex.zabbixmobile.data.ZabbixDataService;
import com.inovex.zabbixmobile.model.Item;

/**
 * Adapter for the list of items used by {@link ChecksApplicationsPage}.
 * 
 */
public class ChecksItemsListAdapter extends BaseServiceAdapter<Item> {

	private static final String TAG = ChecksItemsListAdapter.class
			.getSimpleName();
	private int mTextViewResourceId = R.layout.list_item_items;

	/**
	 * Constructor.
	 * 
	 * @param service
	 * @param textViewResourceId
	 */
	public ChecksItemsListAdapter(ZabbixDataService service) {
		super(service);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			row = getInflater().inflate(mTextViewResourceId, parent, false);

		}

		TextView clock = (TextView) row.findViewById(R.id.item_clock);
		TextView name = (TextView) row.findViewById(R.id.item_name);
		TextView value = (TextView) row.findViewById(R.id.item_value);

		Item i = getItem(position);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(i.getLastClock());
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.SHORT, SimpleDateFormat.SHORT,
				Locale.getDefault());
		clock.setText(dateFormatter.format(cal.getTime()));
		name.setText(i.getDescription());
		value.setText(i.getLastValue() + " " + i.getUnits());

		return row;
	}

	@Override
	public long getItemId(int position) {
		Item item = getItem(position);
		if(item != null)
			return item.getId();
		return 0;
	}

}
