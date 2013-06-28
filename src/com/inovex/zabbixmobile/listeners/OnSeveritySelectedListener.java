package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.TriggerSeverity;

// Container Activity must implement this interface
public interface OnSeveritySelectedListener {
	public void onSeveritySelected(TriggerSeverity severity);
}