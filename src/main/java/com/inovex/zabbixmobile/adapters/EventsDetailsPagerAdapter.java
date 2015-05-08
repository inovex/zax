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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.inovex.zabbixmobile.activities.fragments.EventsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Pager adapter used by {@link EventsDetailsFragment}.
 * 
 */
public class EventsDetailsPagerAdapter extends BaseSeverityPagerAdapter<Event> {

	private static final String TAG = EventsDetailsPagerAdapter.class
			.getSimpleName();

	public EventsDetailsPagerAdapter(TriggerSeverity severity) {
		super(severity);
	}

	public EventsDetailsPagerAdapter(FragmentManager fm,
			TriggerSeverity severity) {
		super(fm, severity);
	}

	@Override
	protected Fragment getItem(int position) {
		EventsDetailsPage f = new EventsDetailsPage();
		Event event = getObject(position);
		f.setEvent(event);
		return f;
	}

	@Override
	public Long getItemId(int position) {
		if (getObject(position) == null)
			return null;
		return getObject(position).getId();
	}

}