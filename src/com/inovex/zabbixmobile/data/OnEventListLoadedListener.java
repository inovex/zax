package com.inovex.zabbixmobile.data;

import java.util.List;

import com.inovex.zabbixmobile.model.Event;

public interface OnEventListLoadedListener {

	public void onEventListLoaded(List<Event> events);
	
}
