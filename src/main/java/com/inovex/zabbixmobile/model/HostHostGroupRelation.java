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

@DatabaseTable(tableName = "host_hostgroup_relation")
public class HostHostGroupRelation {

	private static final String INDEX_HOST_GROUP = "host_group_idx";
	@DatabaseField(generatedId = true)
	private long id;
	/** Trigger ID */
	public static final String COLUMN_HOSTID = "hostid";
	@DatabaseField(uniqueIndexName = INDEX_HOST_GROUP, foreign = true, columnName = COLUMN_HOSTID)
	private Host host;
	/** Host group ID */
	public static final String COLUMN_GROUPID = "groupid";
	@DatabaseField(uniqueIndexName = INDEX_HOST_GROUP, foreign = true, columnName = COLUMN_GROUPID)
	private HostGroup group;

	public HostHostGroupRelation() {

	}

	public HostHostGroupRelation(Host host, HostGroup hostGroup) {
		this.host = host;
		this.group = hostGroup;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public HostGroup getGroup() {
		return group;
	}

	public void setGroup(HostGroup group) {
		this.group = group;
	}

}
