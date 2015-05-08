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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.inovex.zabbixmobile.activities.fragments.ProblemsDetailsFragment;
import com.inovex.zabbixmobile.activities.fragments.ProblemsDetailsPage;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Pager adapter used by {@link ProblemsDetailsFragment}.
 * 
 */
public class ProblemsDetailsPagerAdapter extends
		BaseSeverityPagerAdapter<Trigger> {

	private static final String TAG = ProblemsDetailsPagerAdapter.class
			.getSimpleName();

	public ProblemsDetailsPagerAdapter(TriggerSeverity severity) {
		super(severity);
	}

	public ProblemsDetailsPagerAdapter(FragmentManager fm,
			TriggerSeverity severity) {
		super(fm, severity);
	}

	@Override
	protected Fragment getItem(int position) {
		ProblemsDetailsPage f = new ProblemsDetailsPage();
		Trigger trigger = getObject(position);
		f.setTrigger(trigger);
		return f;
	}

	@Override
	public Long getItemId(int position) {
		if (getObject(position) == null)
			return null;
		return getObject(position).getId();
	}

}