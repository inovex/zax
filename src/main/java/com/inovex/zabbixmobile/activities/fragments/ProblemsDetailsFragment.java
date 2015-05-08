/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

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
