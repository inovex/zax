package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.Event;

public interface OnAcknowledgeEventListener {
	public void acknowledgeEvent(Event event, String comment);
}