package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hosts")
public class Host implements Comparable<Host> {

	public static final String COLUMN_ID = "hostid";
	@DatabaseField(id = true, columnName = COLUMN_ID)
	long id;

	public static final String COLUMN_HOST = "host";
	@DatabaseField(columnName = COLUMN_HOST)
	String name;
	
	HostGroup group;

	// TODO: add group ID if necessary

	public Host() {

	}

	public Host(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setHost(String host) {
		this.name = host;
	}

	public HostGroup getGroup() {
		return group;
	}

	public void setGroup(HostGroup group) {
		this.group = group;
	}

	@Override
	public int compareTo(Host another) {
		if(id == another.getId())
			return 0;
		if(id > another.getId())
			return 1;
		return -1;
	}
}
