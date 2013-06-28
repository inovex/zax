package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "applications")
public class Application {

	public static final String COLUMN_APPLICATIONID = "applicationid";
	@DatabaseField(id = true, columnName = COLUMN_APPLICATIONID)
	private long id;
	public static final String COLUMN_HOSTID = "hostid";
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	Host host;
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	private String name;

	public Application() {

	}

	public long getId() {
		return id;
	}

	public Host getHost() {
		return host;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public void setName(String name) {
		this.name = name;
	}

}
