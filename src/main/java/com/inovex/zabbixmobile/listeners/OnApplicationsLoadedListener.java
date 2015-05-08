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
