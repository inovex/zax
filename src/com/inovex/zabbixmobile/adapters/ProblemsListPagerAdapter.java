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
	public Long getItemId(int position) {
		if(getObject(position) == null)
			return null;
		return (long) getObject(position).getNumber();
	}

}
