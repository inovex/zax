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

import android.content.Context;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;
import com.inovex.zabbixmobile.model.Event;

/**
 * Adapter for pages containing lists of events.
 * 
 */
public class EventsListPagerAdapter extends BaseSeverityListPagerAdapter<Event> {

	public EventsListPagerAdapter(Context context) {
		super(context);
	}

	@Override
	protected BaseSeverityFilterListPage<Event> instantiatePage() {
		return new EventsListPage();
	}

	@Override
	public Long getItemId(int position) {
		if (getObject(position) == null)
			return null;
		return (long) getObject(position).getNumber();
	}

}
