package com.inovex.zabbixmobile.activities.fragments;

import com.inovex.zabbixmobile.model.TriggerSeverities;

// Container Activity must implement this interface
public interface OnEventSelectedListener {

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
	public void onEventSelected(int position, TriggerSeverities severity,
			long id);

}