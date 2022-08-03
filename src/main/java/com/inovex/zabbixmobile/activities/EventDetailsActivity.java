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

package com.inovex.zabbixmobile.activities;

import android.os.Bundle;
import android.util.Log;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterDetailsFragment;
import com.inovex.zabbixmobile.listeners.OnAcknowledgeEventListener;
import com.inovex.zabbixmobile.model.Event;

/**
 * Created by felix on 22/09/15.
 */
public class EventDetailsActivity extends BaseDetailsActivity implements OnAcknowledgeEventListener
{

	private BaseSeverityFilterDetailsFragment mListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events_details);
	}

	@Override
	public void acknowledgeEvent(Event event, String comment) {
		Log.d(TAG, "acknowledgeEvent(" + event + ", " + comment + ")");
		mZabbixDataService.acknowledgeEvent(event, comment, this);
	}

	@Override
	public void onEventAcknowledged() {
		// this refreshes the action bar menu
		mDetailsFragment.refreshItemSelection();
		mDetailsFragment.refreshCurrentItem();
	}
}
