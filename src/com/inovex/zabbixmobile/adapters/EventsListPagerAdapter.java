package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.FragmentManager;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;

public class EventsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public EventsListPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new EventsListPage();
	}

}
