package com.inovex.zabbixmobile.adapters;

import java.util.Arrays;
import java.util.Locale;

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
		BaseSeverityFilterListPage<TriggerSeverity> f = instantiatePage();
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
		return TriggerSeverity.getSeverityByPosition(position).getName().toUpperCase(Locale.getDefault());
	}
	
	protected abstract BaseSeverityFilterListPage<TriggerSeverity> instantiatePage();

}