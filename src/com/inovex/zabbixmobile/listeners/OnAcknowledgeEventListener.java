package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.Event;

public interface OnAcknowledgeEventListener {
	/**
	 * This method performs a Zabbix call to acknowledge the given event.
	 * 
	 * @param event
	 *            the event to be acknowledged
	 * @param comment
	 *            comment
	 */
	public void acknowledgeEvent(Event event, String comment);

	/**
	 * Callback method which is called when acknowledgment has been successful.
	 */
	public void onEventAcknowledged();
}