package com.inovex.zabbixmobile.activities.fragments;

// Container Activity must implement this interface
public interface OnEventSelectedListener {
	public void onEventSelected(int position, int severity, long id);

	public void onEventClicked(int position);
}