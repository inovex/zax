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

package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "screenitems")
public class ScreenItem {

	/** screen Item ID */
	public static final String COLUMN_SCREENITEMID = "screenitemid";
	@DatabaseField(id = true, columnName = COLUMN_SCREENITEMID)
	long id;
	/** screen ID */
	public static final String COLUMN_SCREENID = "screenid";
	@DatabaseField(columnName = COLUMN_SCREENID, index = true)
	long screenId;
	/** graph id */
	public static final String COLUMN_RESOURCEID = "resourceid";
	@DatabaseField(columnName = COLUMN_RESOURCEID)
	long resourceId;

	public ScreenItem() {

	}

	public long getId() {
		return id;
	}

	public long getScreenId() {
		return screenId;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setScreenId(long screenId) {
		this.screenId = screenId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

}
