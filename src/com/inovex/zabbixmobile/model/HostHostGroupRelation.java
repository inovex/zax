package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="host_hostgroup_relation")
public class HostHostGroupRelation {

	@DatabaseField(generatedId = true)
	private long id;
	/** Trigger ID */
	public static final String COLUMN_HOSTID = "hostid";
	@DatabaseField(uniqueIndexName = "host_group_idx", foreign = true, columnName = COLUMN_HOSTID)
	private Host host;
	/** Host group ID */
	public static final String COLUMN_GROUPID = "groupid";
	@DatabaseField(uniqueIndexName = "host_group_idx", foreign = true, columnName = COLUMN_GROUPID)
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
