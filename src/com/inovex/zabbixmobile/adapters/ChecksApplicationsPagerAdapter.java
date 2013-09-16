package com.inovex.zabbixmobile.adapters;

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
		return getObject(position).getName();
	}

	@Override
	public Long getItemId(int position) {
		if(getObject(position) == null)
			return null;
		return getObject(position).getId();
	}

	@Override
	protected Fragment getItem(int position) {
		ChecksApplicationsPage p = new ChecksApplicationsPage();
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
