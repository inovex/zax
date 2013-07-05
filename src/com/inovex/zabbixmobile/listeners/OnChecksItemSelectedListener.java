package com.inovex.zabbixmobile.listeners;

// Container Activity must implement this interface
public interface OnChecksItemSelectedListener {

	/**
	 * Callback method for the selection of an item.
	 * 
	 * @param position
	 *            list position
	 * @param id
	 *            event ID (Zabbix event_id)
	 */
	public void onHostSelected(int position, long id);

	/**
	 * Callback method for the selection of an item.
	 * 
	 * @param position
	 *            list position
	 * @param id
	 *            event ID (Zabbix event_id)
	 */
	public void onItemSelected(int position, long id);

}