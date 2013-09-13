package com.inovex.zabbixmobile.activities.fragments;

import android.os.Bundle;
import android.view.View;

import com.inovex.zabbixmobile.R;
import com.inovex.zabbixmobile.model.Trigger;

/**
 * Fragment displaying a list of problems for a particular severity.
 *
 */
public class ProblemsListPage extends BaseSeverityFilterListPage<Trigger> {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setCustomEmptyText(getResources().getString(
				R.string.empty_list_problems));
	}

	@Override
	protected void setupListAdapter() {
		mListAdapter = mZabbixDataService.getProblemsListAdapter(mSeverity);
		setListAdapter(mListAdapter);
	}

}
