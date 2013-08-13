package com.inovex.zabbixmobile.adapters;

import android.content.Context;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;

public class ProblemsListPagerAdapter extends BaseSeverityListPagerAdapter {

	public ProblemsListPagerAdapter(Context context) {
		super(context);
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
