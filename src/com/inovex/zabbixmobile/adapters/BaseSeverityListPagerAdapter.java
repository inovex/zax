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

	public BaseSeverityListPagerAdapter(Context context) {
		super();
		mContext = context;
		addAll(Arrays.asList(TriggerSeverity.values()));
		notifyDataSetChanged();
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
				.toUpperCase(Locale.getDefault());
	}

	protected abstract BaseSeverityFilterListPage<T> instantiatePage();

}