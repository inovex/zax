package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.Fragment;

import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsPage;
import com.inovex.zabbixmobile.model.Application;

public class ChecksApplicationsPagerAdapter extends
		BaseServicePagerAdapter<Application> {

	@Override
	public CharSequence getPageTitle(int position) {
		return getItem(position).getName();
		// return ((ChecksDetailsPage)getPage(position)).getTitle();
		// return "TITLE";
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	protected Fragment getPage(int position) {
		ChecksDetailsPage p = new ChecksDetailsPage();
		p.setApplication(getItem(position));
		return p;
	}
	
	public ChecksDetailsPage getCurrentPage() {
		return (ChecksDetailsPage) getPage(getCurrentPosition());
	}

	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}

}
