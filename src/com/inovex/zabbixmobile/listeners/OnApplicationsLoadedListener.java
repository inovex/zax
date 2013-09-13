package com.inovex.zabbixmobile.listeners;

public interface OnApplicationsLoadedListener {

	/**
	 * Called when all applications have been loaded.
	 */
	public void onApplicationsLoaded();

	/**
	 * Called upon loading progress update.
	 * 
	 * @param progress
	 *            the current progress
	 */
	public void onApplicationsProgressUpdate(int progress);

}
