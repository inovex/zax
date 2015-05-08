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

import com.inovex.zabbixmobile.model.Item;

// Container Activity must implement this interface
public interface OnChecksItemSelectedListener {

	/**
	 * Callback method for the selection of a host.
	 * 
	 * @param position
	 *            list position
	 * @param id
	 *            event ID (Zabbix event_id)
	 */
	public void onHostSelected(int position, long id);

	/**
	 * Callback method for the selection of an application.
	 * 
	 * @param position
	 *            list position
	 */
	public void onApplicationSelected(int position);

	/**
	 * Callback method for the selection of an item.
	 * 
	 * @param position
	 *            list position
	 * @param item
	 *            the selected item
	 * @param showItemDetails
	 *            whether or not the item details shall be shown (at activity
	 *            startup, the details shall not be shown)
	 */
	public void onItemSelected(int position, Item item, boolean showItemDetails);

}