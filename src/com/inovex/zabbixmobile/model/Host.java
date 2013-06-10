package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hosts")
public class Host {

	@DatabaseField(id = true)
	long id;
	@DatabaseField
	String host;

	// TODO: add group ID if necessary

	public Host() {

	}

	public Host(long id, String name) {
		this.id = id;
		this.host = name;
	}
}
