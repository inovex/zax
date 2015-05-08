/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

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
