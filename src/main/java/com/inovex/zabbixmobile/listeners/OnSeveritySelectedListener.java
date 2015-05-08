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

public interface OnSeveritySelectedListener {

	/**
	 * Called when a particular severity has been selected.
	 * 
	 * @param severity
	 *            the selected severity
	 */
	public void onSeveritySelected(TriggerSeverity severity);
}