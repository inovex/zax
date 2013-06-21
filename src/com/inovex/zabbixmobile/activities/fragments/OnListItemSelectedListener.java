package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.model.TriggerSeverity;

// Container Activity must implement this interface
public interface OnListItemSelectedListener {

	/**
	 * Callback method for the selection of an event.
	 * 
	 * @param position
	 *            list position
	 * @param severity
	 *            severity of the event
	 * @param id
	 *            event ID (Zabbix event_id)
	 */
	public void onListItemSelected(int position, TriggerSeverity severity,
			long id);

}