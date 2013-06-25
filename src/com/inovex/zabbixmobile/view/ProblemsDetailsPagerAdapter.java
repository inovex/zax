package com.inovex.zabbixmobile.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.inovex.zabbixmobile.activities.fragments.EventsDetailsPage;
import com.inovex.zabbixmobile.activities.fragments.ProblemsDetailsPage;
import com.inovex.zabbixmobile.model.Event;
import com.inovex.zabbixmobile.model.Trigger;
import com.inovex.zabbixmobile.model.TriggerSeverity;

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

}