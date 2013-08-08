package com.inovex.zabbixmobile.adapters;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;

public class ProblemsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public ProblemsListPagerAdapter() {
		super();
	}

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new ProblemsListPage();
	}

	@Override
	public long getItemId(int position) {
		return getObject(position).getNumber();
	}

}
