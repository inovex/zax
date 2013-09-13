package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.TriggerSeverity;

public interface OnSeveritySelectedListener {

	/**
	 * Called when a particular severity has been selected.
	 * 
	 * @param severity
	 *            the selected severity
	 */
	public void onSeveritySelected(TriggerSeverity severity);
}