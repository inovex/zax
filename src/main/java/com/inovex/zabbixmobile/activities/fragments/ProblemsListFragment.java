package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Fragment displaying several lists of problems (one for each severity) using a
 * view pager.
 * 
 */
public class ProblemsListFragment extends
		BaseSeverityFilterListFragment<Trigger> {

	public static final String TAG = ProblemsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityListPagerAdapter<Trigger> retrievePagerAdapter() {
		return mZabbixDataService.getProblemsListPagerAdapter();
	}

}
