package com.inovex.zabbixmobile.adapters;

import java.util.Arrays;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.model.TriggerSeverity;

public abstract class BaseSeverityListPagerAdapter extends BaseServicePagerAdapter<TriggerSeverity> {

	public BaseSeverityListPagerAdapter() {
		super();
		addAll(Arrays.asList(TriggerSeverity.values()));
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int i) {
		BaseSeverityFilterListPage f = instantiatePage();
		f.setSeverity(TriggerSeverity.getSeverityByPosition(i));
		Log.d(BaseSeverityFilterListFragment.TAG, "getItem: " + f.toString());
		return f;
	}

	@Override
	public int getCount() {
		return TriggerSeverity.values().length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TriggerSeverity.getSeverityByPosition(position).getName();
	}
	
	protected abstract BaseSeverityFilterListPage instantiatePage();

}