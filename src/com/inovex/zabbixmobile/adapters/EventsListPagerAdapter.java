package com.inovex.zabbixmobile.adapters;

import android.content.Context;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;

public class EventsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public EventsListPagerAdapter(Context context) {
		super(context);
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
