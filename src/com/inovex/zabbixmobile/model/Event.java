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

	@DatabaseField(id = true)
	long id;
	@DatabaseField
	int source;
	@DatabaseField(canBeNull = true, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
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

	public Event(long id, int source, long timestamp, int value,
			boolean acknowledged, boolean value_changed) {
		this.id = id;
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

	public String getDetailedString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("source=").append(source);
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT);
		sb.append(", ").append("date=").append(dateFormatter.format(clock));
		sb.append(", ").append("value=").append(value);
		sb.append(", ").append("acknowledged=").append(acknowledged);
		sb.append(", ").append("value_changed=").append(value_changed);
		sb.append(", ").append("trigger={").append(trigger).append("}");
		return sb.toString();
	}

	public long getId() {
		return id;
	}

	public int getSource() {
		return source;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public Date getClock() {
		return clock;
	}

	public int getValue() {
		return value;
	}

	public boolean isAcknowledged() {
		return acknowledged;
	}

	public boolean isValue_changed() {
		return value_changed;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
