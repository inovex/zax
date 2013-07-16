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
	private ArrayList<ChecksDetailsPage> instantiatedPages = new ArrayList<ChecksDetailsPage>();
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

	public ChecksDetailsPage getCurrentPage() {
		return instantiatedPages.get(getCurrentPosition());
	}

	@Override
	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Object instantiatedItem = super
				.instantiateItem(container, position);
		// save instantiated page
		Log.d(TAG, "instantiateItem: " + instantiatedItem.toString());
		instantiatedPages
				.add((ChecksDetailsPage) instantiatedItem);
		return instantiatedItem;
	}

	/**
	 * Returns all pages in this view pager which have already been
	 * instantiated.
	 * 
	 * @return instantiated pages
	 */
	public Collection<ChecksDetailsPage> getPages() {
		return instantiatedPages;
	}

	public void setLoadingSpinnerVisible(boolean loadingSpinnerVisible) {
		this.mLoadingSpinnerVisible = loadingSpinnerVisible;
	}

}
