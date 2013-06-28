package com.inovex.zabbixmobile.listeners;

// Container Activity must implement this interface
public interface OnListItemSelectedListener {

	/**
	 * Callback method for the selection of an item.
	 * 
	 * @param position
	 *            list position
	 * @param id
	 *            event ID (Zabbix event_id)
	 */
	public void onListItemSelected(int position, long id);

}