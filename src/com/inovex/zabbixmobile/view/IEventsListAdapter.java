package com.inovex.zabbixmobile.view;

import java.util.Collection;

import com.inovex.zabbixmobile.model.Event;

/**
 * Interface for list adapters containing events.
 * 
 */
public interface IEventsListAdapter {

	/**
	 * Adds a collection of events to the adapter.
	 * 
	 * @param events
	 *            collection of events to be added
	 */
	public void addAll(Collection<? extends Event> events);

	/**
	 * Needs to be called when the data set has been changed.
	 */
	public void notifyDataSetChanged();
}
