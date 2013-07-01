package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "items")
public class Item implements Comparable<Item> {

	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(id = true, columnName = COLUMN_ITEMID)
	long id;
	public static final String COLUMN_HOSTID = "hostid";
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = COLUMN_HOSTID)
	Host host;
	public static final String COLUMN_VALUE_TYPE = "value_type";
	@DatabaseField(columnName = COLUMN_VALUE_TYPE)
	int valueType;
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_DESCRIPTION_OLD = "name";
	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	String description;
	public static final String COLUMN_LASTCLOCK = "lastclock";
	@DatabaseField(columnName = COLUMN_LASTCLOCK)
	long lastClock;
	public static final String COLUMN_LASTVALUE = "lastvalue";
	@DatabaseField(columnName = COLUMN_LASTVALUE)
	String lastValue;
	public static final String COLUMN_UNITS = "units";
	@DatabaseField(columnName = COLUMN_UNITS)
	String units;

	public Item() {

	}

	public Item(long id, Host host, int valueType, String description,
			long lastClock, String lastValue, String units) {
		this.id = id;
		this.host = host;
		this.valueType = valueType;
		this.description = description;
		this.lastClock = lastClock;
		this.lastValue = lastValue;
		this.units = units;
	}

	public long getId() {
		return id;
	}

	public Host getHost() {
		return host;
	}

	public int getValueType() {
		return valueType;
	}

	public String getDescription() {
		return description;
	}

	public long getLastClock() {
		return lastClock;
	}

	public String getLastValue() {
		return lastValue;
	}

	public String getUnits() {
		return units;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public void setValueType(int valueType) {
		this.valueType = valueType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLastClock(long lastClock) {
		this.lastClock = lastClock;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public String toString() {
		return "item " + getId() + ": " + getDescription();
	}

	@Override
	public int compareTo(Item another) {
		if(this.id > another.getId())
			return 1;
		return -1;
	}

}
