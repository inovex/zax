package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.Fragment;

import com.inovex.zabbixmobile.activities.fragments.ChecksItemsDetailsPage;
import com.inovex.zabbixmobile.model.Item;

public class ChecksItemsPagerAdapter extends BaseServicePagerAdapter<Item> {

	@Override
	public CharSequence getPageTitle(int position) {
		if (position == mCurrentPosition - 1)
			return "previous";
		if (position == mCurrentPosition + 1)
			return "next";
		return (position + 1) + "/" + getCount();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	protected Fragment getPage(int position) {
		ChecksItemsDetailsPage p = new ChecksItemsDetailsPage();
		p.setItem(getItem(position));
		return p;
	}

	public ChecksItemsDetailsPage getCurrentPage() {
		return (ChecksItemsDetailsPage) getPage(getCurrentPosition());
	}

	@Override
	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}

}
