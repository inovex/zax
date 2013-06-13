package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.model.TriggerSeverities;

// Container Activity must implement this interface
public interface OnEventSelectedListener {
	public void onEventSelected(int position, TriggerSeverities severity, long id);

}