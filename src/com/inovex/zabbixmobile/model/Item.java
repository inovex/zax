package com.inovex.zabbixmobile.model;

import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "items")
public class Item {

	@DatabaseField(id = true)
	long id;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	Host host;
	@DatabaseField
	int value_type;
	@DatabaseField
	String description;
	@DatabaseField
	Date lastclock;
	@DatabaseField
	String lastvalue;
	@DatabaseField
	String units;

	public Item() {

	}

	public Item(long id, Host host, int valueType, String description,
			long lastclock, String lastvalue, String units) {
		this.id = id;
		this.host = host;
		this.value_type = valueType;
		this.description = description;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(lastclock);
		this.lastclock = cal.getTime();
		this.lastvalue = lastvalue;
		this.units = units;
	}
}
