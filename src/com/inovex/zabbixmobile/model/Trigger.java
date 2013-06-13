package com.inovex.zabbixmobile.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "triggers")
public class Trigger {

	@DatabaseField(id = true)
	long id;
	@DatabaseField
	String description;
	@DatabaseField
	String expression;
	@DatabaseField
	String comments;
	@DatabaseField
	Date lastChange;
	@DatabaseField
	TriggerSeverities priority;
	@DatabaseField
	int status;
	@DatabaseField
	int value;
	@DatabaseField
	String url;
	@DatabaseField
	boolean valueChanged;
	
//	@ForeignCollectionField(eager = false)
	ForeignCollection<Host> hosts;
//	@ForeignCollectionField(eager = false)
	ForeignCollection<Item> items;

	public Trigger() {

	}

	public Trigger(long id, String description, String expression,
			String comments, long lastChange, TriggerSeverities priority, int status,
			int value, String url, boolean valueChanged) {
		this.id = id;
		this.description = description;
		this.expression = expression;
		this.comments = comments;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(lastChange);
		this.lastChange = cal.getTime();
		this.priority = priority;
		this.status = status;
		this.value = value;
		this.url = url;
		this.valueChanged = valueChanged;
	}

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getExpression() {
		return expression;
	}

	public String getComments() {
		return comments;
	}

	public Date getLastchange() {
		return lastChange;
	}

	public TriggerSeverities getPriority() {
		return priority;
	}

	public int getStatus() {
		return status;
	}

	public int getValue() {
		return value;
	}

	public String getUrl() {
		return url;
	}

	public boolean isValueChanged() {
		return valueChanged;
	}

	public ForeignCollection<Host> getHosts() {
		return hosts;
	}

	public ForeignCollection<Item> getItems() {
		return items;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("description=").append(description);
		sb.append(", ").append("expression=").append(expression);
		sb.append(", ").append("comments=").append(comments);
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance(
				SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, Locale.US);
		sb.append(", ").append("lastchange=").append(dateFormatter.format(lastChange));
		sb.append(", ").append("priority=").append(priority);
		sb.append(", ").append("status=").append(status);
		sb.append(", ").append("value=").append(value);
		sb.append(", ").append("url=").append(url);
		sb.append(", ").append("valueChanged=").append(valueChanged);
		return sb.toString();
	}

}
