package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.adapters.ProblemsDetailsPagerAdapter;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Fragment which displays event details using a ViewPager (adapter:
 * {@link ProblemsDetailsPagerAdapter}).
 * 
 */
public class ProblemsDetailsFragment extends
		BaseSeverityFilterDetailsFragment<Trigger> {

	public static final String TAG = ProblemsDetailsFragment.class
			.getSimpleName();

	@Override
	protected void retrievePagerAdapter() {
		mDetailsPagerAdapter = mZabbixDataService
				.getProblemsDetailsPagerAdapter(mSeverity);
	}

	@Override
	public void refreshCurrentItem() {
		ProblemsDetailsPage currentPage = (ProblemsDetailsPage) mDetailsPagerAdapter
				.instantiateItem(mDetailsPager,
						mDetailsPagerAdapter.getCurrentPosition());
		if (currentPage != null)
			currentPage.refresh();
	}

}
