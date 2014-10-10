package com.inovex.zabbixmobile.adapters;

import android.content.Context;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsListPage;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Adapter for pages containing lists of problems.
 * 
 */
public class ProblemsListPagerAdapter extends
		BaseSeverityListPagerAdapter<Trigger> {

	public ProblemsListPagerAdapter(Context context) {
		super(context);
	}

	@Override
	protected BaseSeverityFilterListPage<Trigger> instantiatePage() {
		return new ProblemsListPage();
	}

	@Override
	public Long getItemId(int position) {
		if (getObject(position) == null)
			return null;
		return (long) getObject(position).getNumber();
	}

}
