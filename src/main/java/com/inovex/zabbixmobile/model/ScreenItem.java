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

	private static final String INDEX_SCREENITEMID_HOST = "screenitem_host_idx";
	/** row ID */
	public static final String COLUMN_ID = "id";
	@DatabaseField(generatedId = true, columnName = COLUMN_ID)
	long id;
	/** screen Item ID */
	public static final String COLUMN_SCREENITEMID = "screenitemid";
	@DatabaseField(uniqueIndexName = INDEX_SCREENITEMID_HOST, columnName = COLUMN_SCREENITEMID)
	long screenItemId;
	/** screen ID */
	public static final String COLUMN_SCREENID = "screenid";
	@DatabaseField(columnName = COLUMN_SCREENID, index = true)
	long screenId;
	/** graph id */
	public static final String COLUMN_RESOURCEID = "resourceid";
	@DatabaseField(columnName = COLUMN_RESOURCEID)
	long resourceId;
	/** host (used for template screens) */
	public static final String COLUMN_HOST = "host";
	@DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueIndexName = INDEX_SCREENITEMID_HOST, columnName = COLUMN_HOST)
	Host host;
	/** real graph id (used for template screens) */
	public static final String COLUMN_REAL_RESOURCEID = "real_resourceid";
	@DatabaseField(columnName = COLUMN_REAL_RESOURCEID)
	Long realResourceId;

	public ScreenItem() {

	}

	public long getScreenItemId() {
		return screenItemId;
	}

	public long getScreenId() {
		return screenId;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setScreenItemId(long screenItemId) {
		this.screenItemId = screenItemId;
	}

	public void setScreenId(long screenId) {
		this.screenId = screenId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public Long getRealResourceId() {
		return realResourceId;
	}

	public void setRealResourceId(Long realResourceId) {
		this.realResourceId = realResourceId;
	}
}
