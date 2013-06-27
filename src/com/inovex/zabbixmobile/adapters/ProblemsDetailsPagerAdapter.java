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
public class ProblemsDetailsPagerAdapter extends BaseSeverityPagerAdapter<Trigger> {

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
	protected Fragment getPage(int position) {
		ProblemsDetailsPage f = new ProblemsDetailsPage();
		Trigger trigger = getItem(position);
		f.setTrigger(trigger);
		return f;
	}
	
	public long getItemId(int position) {
		return getItem(position).getId();
	}

}