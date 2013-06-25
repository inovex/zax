package com.inovex.zabbixmobile.activities.fragments;


/**
 * Fragment displaying several lists of problems (one for each severity) using a
 * view pager.
 * 
 */
public class ProblemsListFragment extends BaseSeverityFilterListFragment {

	public static final String TAG = ProblemsListFragment.class.getSimpleName();

	@Override
	protected BaseSeverityFilterListPage instantiatePage() {
		return new ProblemsListPage();
	}

}
