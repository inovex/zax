package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.Screen;

// Container Activity must implement this interface
public interface OnScreensItemSelectedListener {

	/**
	 * Callback method for the selection of a screen.
	 * 
	 * @param screen
	 *            the screen
	 */
	public void onScreenSelected(Screen screen);

}