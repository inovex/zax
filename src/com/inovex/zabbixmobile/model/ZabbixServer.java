package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "zabbixservers")
public class ZabbixServer implements Comparable<ZabbixServer> {

	/** Screen ID */
	public static final String COLUMN_ZABBIXSERVERID = "zabbixserverid";
	@DatabaseField(columnName = COLUMN_ZABBIXSERVERID, generatedId = true)
	long id;

	/** Screen name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	String name;

	public ZabbixServer() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(long serverId) {
		this.id = serverId;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(ZabbixServer another) {
		return this.getName().compareTo(another.getName());
	}

}
