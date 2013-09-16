package com.inovex.zabbixmobile.listeners;

public interface OnApplicationsLoadedListener {

	/**
	 * Called when all applications have been loaded.
	 * 
	 * @param resetSelection
	 *            flag indication whether the application selection shall be
	 *            reverted
	 */
	public void onApplicationsLoaded(boolean resetSelection);

	/**
	 * Called upon loading progress update.
	 * 
	 * @param progress
	 *            the current progress
	 */
	public void onApplicationsProgressUpdate(int progress);

}
