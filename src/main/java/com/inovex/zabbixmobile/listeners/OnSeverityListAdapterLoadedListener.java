package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.TriggerSeverity;

public interface OnSeverityListAdapterLoadedListener {

	/**
	 * Called when an adapter's content has been loaded. This method should take
	 * care of dismissing any loading bars.
	 * 
	 * @param severity
	 *            severity of the loaded data items.
	 * @param hostGroupChanged
	 *            whether or not the host group has been changed (this is
	 *            necessary because the item selection shall not be retained
	 *            when the host group has been changed)
	 */
	public void onSeverityListAdapterLoaded(TriggerSeverity severity,
			boolean hostGroupChanged);

	/**
	 * Updates the progress of loading an adapter's content.
	 * 
	 * @param progress
	 *            the current progress
	 */
	public void onSeverityListAdapterProgressUpdate(int progress);
}
