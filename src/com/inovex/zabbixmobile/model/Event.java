package com.inovex.zabbixmobile.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@DatabaseTable(tableName = "events")
public class Event {

	// TODO: For the moment, we have the id generated; in the real application, we'll get it from Zabbix
	//@DatabaseField(id = true) 
	@DatabaseField(generatedId = true)
	long id;
	@DatabaseField
	int source;
	@DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
	Trigger trigger;
	@DatabaseField
	Date clock;
	@DatabaseField
	int value;
	@DatabaseField
	boolean acknowledged;
	@DatabaseField
	boolean value_changed;
	
	Event() {
		// needed by ormlite
	}

	public Event(long id, int source, long timestamp, int value, boolean acknowledged, boolean value_changed) {
		// this.id = id;
		this.source = source;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		this.clock = cal.getTime();
		this.value = value;
		this.acknowledged = acknowledged;
		this.value_changed = value_changed;
	}
	
	public void setTrigger(Trigger t) {
		trigger = t;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("source=").append(source);
		sb.append(", ").append("trigger=").append(trigger);
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, Locale.US);
		sb.append(", ").append("date=").append(dateFormatter.format(clock));
		sb.append(", ").append("value=").append(value);
		sb.append(", ").append("acknowledged=").append(acknowledged);
		sb.append(", ").append("value_changed=").append(value_changed);
		return sb.toString();
	}
}
