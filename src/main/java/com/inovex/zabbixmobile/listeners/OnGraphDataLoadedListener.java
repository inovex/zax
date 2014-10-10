package com.inovex.zabbixmobile.listeners;

public interface OnGraphDataLoadedListener {

	/**
	 * Called when a graph's data has been completely loaded.
	 */
	public void onGraphDataLoaded();

	/**
	 * Called upon a loading progress update.
	 * 
	 * @param progress
	 *            the current loading progress
	 */
	public void onGraphDataProgressUpdate(int progress);

}
