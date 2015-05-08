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

@DatabaseTable(tableName = "hostgroups")
public class HostGroup implements Comparable<HostGroup> {

	public static final long GROUP_ID_ALL = -1;

	/** Host ID */
	public static final String COLUMN_GROUPID = "groupid";
	@DatabaseField(id = true, columnName = COLUMN_GROUPID)
	private long groupId;

	/** Host name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	private String name;

	/** zabbix server */
	public static final String COLUMN_ZABBIXSERVER_ID = "zabbixserverid";
	@DatabaseField(columnName = COLUMN_ZABBIXSERVER_ID)
	private Long zabbixServerId;

	public HostGroup() {

	}

	public HostGroup(long groupId, String name) {
		this.groupId = groupId;
		this.name = name;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return groupId + " " + name;
	}

	@Override
	public int compareTo(HostGroup another) {
		if (groupId > another.getGroupId())
			return 1;
		if (groupId < another.getGroupId())
			return -1;
		return 0;
	}

	public Long getZabbixServerId() {
		return zabbixServerId;
	}

	public void setZabbixServerId(Long zabbixServerId) {
		this.zabbixServerId = zabbixServerId;
	}

}
