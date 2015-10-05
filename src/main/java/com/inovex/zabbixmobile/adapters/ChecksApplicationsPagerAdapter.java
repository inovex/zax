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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.inovex.zabbixmobile.activities.fragments.ChecksApplicationsPage;
import com.inovex.zabbixmobile.model.Application;

/**
 * Pager adapter containing application pages.
 * 
 */
public class ChecksApplicationsPagerAdapter extends
		BaseServicePagerAdapter<Application> {

	private static final String TAG = ChecksApplicationsPagerAdapter.class
			.getSimpleName();

	@Override
	public CharSequence getPageTitle(int position) {
		Application application = getObject(position);
		if(application != null){
			return application.getName();
		} else {
			return  "";
		}
	}

	@Override
	public Long getItemId(int position) {
		if (getObject(position) == null)
			return null;
		return getObject(position).getId();
	}

	@Override
	protected Fragment getItem(int position) {
		ChecksApplicationsPage p = new ChecksApplicationsPage();
		Bundle args = new Bundle();
		args.putLong("applicationID",getObject(position).getId());
		p.setArguments(args);
		return p;
	}

	@Override
	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}

}
