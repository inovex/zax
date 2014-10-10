package com.inovex.zabbixmobile.adapters;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.inovex.zabbixmobile.model.TriggerSeverity;

/**
 * Base class for a pager adapter containing details pages for a particular
 * severity (see {@link TriggerSeverity}.
 * 
 * @param <T>
 *            class of the items in this adapter's data set
 */
public abstract class BaseSeverityPagerAdapter<T> extends
		BaseServicePagerAdapter<T> {

	private static final String TAG = BaseSeverityPagerAdapter.class
			.getSimpleName();

	/**
	 * Creates an adapter.
	 * 
	 * @param severity
	 *            severity of this adapter
	 */
	public BaseSeverityPagerAdapter(TriggerSeverity severity) {
		this(null, severity);
	}

	/**
	 * Creates an adapter
	 * 
	 * @param fm
	 *            the fragment manager to be used by this view pager.
	 * @param severity
	 *            severity of this adapter
	 */
	public BaseSeverityPagerAdapter(FragmentManager fm, TriggerSeverity severity) {
		super();
		mFragmentManager = fm;
		Log.d(TAG, "creating SeverityPagerAdapter for severity " + severity);

	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position == mCurrentPosition - 1)
			return "Newer";
		else if (position == mCurrentPosition + 1)
			return "Older";
		return (position + 1) + " of " + getCount();
	}

	@Override
	public int getItemPosition(Object object) {
		// This prevents caching of fragments. We need to disable caching
		// because we have only one adapter which is reused when another host is
		// selected.
		return POSITION_NONE;
	}

}
