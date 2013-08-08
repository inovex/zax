package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.BaseSeverityListPagerAdapter;
import com.inovex.zabbixmobile.adapters.ProblemsListPagerAdapter;

/**
 * Fragment displaying several lists of problems (one for each severity) using a
 * view pager.
 * 
 */
public class ProblemsListFragment extends BaseSeverityFilterListFragment {

	public static final String TAG = ProblemsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityListPagerAdapter createPagerAdapter() {
		return new ProblemsListPagerAdapter(getChildFragmentManager());
	}

}
