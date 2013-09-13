package com.inovex.zabbixmobile.listeners;

public interface OnGraphsLoadedListener {

	/**
	 * Called when one or several graphs have been completely loaded.
	 */
	public void onGraphsLoaded();

	/**
	 * Called upon a loading progress update.
	 * 
	 * @param progress
	 *            the current progress
	 */
	public void onGraphsProgressUpdate(int progress);

}
