package com.inovex.zabbixmobile.adapters;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;

public class EventsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public EventsListPagerAdapter() {
		super();
	}

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new EventsListPage();
	}

	@Override
	public long getItemId(int position) {
		return getObject(position).getNumber();
	}

}
