package com.inovex.zabbixmobile.listeners;

public interface OnAcknowledgeEventListener {
	public void acknowledgeEvent(long eventId, String comment);
}