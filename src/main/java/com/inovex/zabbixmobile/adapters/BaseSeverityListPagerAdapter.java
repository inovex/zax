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

package com.inovex.zabbixmobile.adapters;

import java.util.Arrays;
import java.util.Locale;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListFragment;
import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Base class for a pager adapter containing one list page for each severity.
 * 
 */
public abstract class BaseSeverityListPagerAdapter<T> extends
		BaseServicePagerAdapter<TriggerSeverity> {

	protected Context mContext;
	protected String[] numElements;

	public BaseSeverityListPagerAdapter(Context context) {
		super();
		mContext = context;
		addAll(Arrays.asList(TriggerSeverity.values()));
		notifyDataSetChanged();
		numElements = new String[TriggerSeverity.values().length];
		for (int i = 0; i < TriggerSeverity.values().length; i++) {
			numElements[i] = "...";
		}
	}

	@Override
	public Fragment getItem(int i) {
		BaseSeverityFilterListPage<T> f = instantiatePage();
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
		return mContext
				.getResources()
				.getString(
						TriggerSeverity.getSeverityByPosition(position)
								.getNameResourceId())
				.toUpperCase(Locale.getDefault())
				+ " (" + numElements[position] + ")";
	}

	protected abstract BaseSeverityFilterListPage<T> instantiatePage();

	public void updateTitle(int position, int count) {
		if (numElements.length > position)
			numElements[position] = String.valueOf(count);
	}

}