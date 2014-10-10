package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.ZabbixServer;

public interface OnServerSelectedListener {

	/**
	 * Called when a particular server has been selected.
	 * 
	 * @param server
	 *            the selected server
	 */
	public void onServerSelected(ZabbixServer server);
}