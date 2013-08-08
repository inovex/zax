package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.FragmentManager;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.EventsListPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;

public class ProblemsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public ProblemsListPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new ProblemsListPage();
	}

}
