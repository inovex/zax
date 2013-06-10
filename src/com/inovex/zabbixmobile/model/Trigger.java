package com.inovex.zabbixmobile.model;

import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "triggers")
public class Trigger {

	// TODO: For the moment, we have the id generated; in the real application,
	// we'll get it from Zabbix
	// @DatabaseField(id = true)
	@DatabaseField(generatedId = true)
	long id;
	@DatabaseField
	String description;
	@DatabaseField
	String expression;
	@DatabaseField
	String comments;
	@DatabaseField
	Date lastchange;
	@DatabaseField
	int priority;
	@DatabaseField
	int status;
	@DatabaseField
	int value;
	@DatabaseField
	String url;
	@DatabaseField
	boolean value_changed;
	@ForeignCollectionField(eager = false)
	ForeignCollection<Host> hosts;
	@ForeignCollectionField(eager = false)
	ForeignCollection<Item> items;

	public Trigger() {

	}

	public Trigger(long id, String description, String expression,
			String comments, long lastchange, int priority, int status,
			int value, String url, boolean value_changed) {
//		this.id = id;
		this.description = description;
		this.expression = expression;
		this.comments = comments;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(lastchange);
		this.lastchange = cal.getTime();
		this.priority = priority;
		this.status = status;
		this.value = value;
		this.url = url;
		this.value_changed = value_changed;
	}

}
