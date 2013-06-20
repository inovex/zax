package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hosts")
public class Host {

	public static final String COLUMN_ID = "hostid";
	@DatabaseField(id = true, columnName = COLUMN_ID)
	long id;

	public static final String COLUMN_HOST = "host";
	@DatabaseField(columnName = COLUMN_HOST)
	String host;
	
	HostGroup group;

	// TODO: add group ID if necessary

	public Host() {

	}

	public Host(long id, String name) {
		this.id = id;
		this.host = name;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public HostGroup getGroup() {
		return group;
	}

	public void setGroup(HostGroup group) {
		this.group = group;
	}
}
