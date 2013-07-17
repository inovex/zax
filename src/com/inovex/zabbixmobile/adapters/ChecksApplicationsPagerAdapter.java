package com.inovex.zabbixmobile.adapters;

import java.util.ArrayList;
import java.util.Collection;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ViewGroup;

import com.inovex.zabbixmobile.activities.fragments.BaseSeverityFilterListPage;
import com.inovex.zabbixmobile.activities.fragments.ChecksDetailsPage;
import com.inovex.zabbixmobile.model.Application;

public class ChecksApplicationsPagerAdapter extends
		BaseServicePagerAdapter<Application> {

	private static final String TAG = ChecksApplicationsPagerAdapter.class.getSimpleName();
	private boolean mLoadingSpinnerVisible = true;
	
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
//		if(!mLoadingSpinnerVisible )
//			p.dismissLoadingSpinner();
		return p;
	}
	
	@Override
	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}
	
	public void setLoadingSpinnerVisible(boolean loadingSpinnerVisible) {
		this.mLoadingSpinnerVisible = loadingSpinnerVisible;
	}

}
