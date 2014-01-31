package com.inovex.zabbixmobile.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "triggers")
public class Trigger implements Comparable<Trigger> {

	public static final int VALUE_PROBLEM = 1;
	public static final int VALUE_OK = 0;
	public static int STATUS_ENABLED = 0;

	public static final String COLUMN_TRIGGERID = "triggerid";
	@DatabaseField(id = true, columnName = COLUMN_TRIGGERID)
	long id;
	public static final String COLUMN_DESCRIPTION = "description";
	@DatabaseField(columnName = COLUMN_DESCRIPTION)
	String description;
	public static final String COLUMN_EXPRESSION = "expression";
	@DatabaseField(columnName = COLUMN_EXPRESSION)
	String expression;
	public static final String COLUMN_COMMENTS = "comments";
	@DatabaseField(columnName = COLUMN_COMMENTS)
	String comments;
	public static final String COLUMN_LASTCHANGE = "lastchange";
	@DatabaseField(columnName = COLUMN_LASTCHANGE)
	long lastChange;
	public static final String COLUMN_PRIORITY = "priority";
	@DatabaseField(columnName = COLUMN_PRIORITY, index = true)
	TriggerSeverity priority;
	public static final String COLUMN_STATUS = "status";
	@DatabaseField(columnName = COLUMN_STATUS)
	int status;
	public static final String COLUMN_VALUE = "value";
	@DatabaseField(columnName = COLUMN_VALUE)
	int value;
	public static final String COLUMN_URL = "url";
	@DatabaseField(columnName = COLUMN_URL)
	String url;
	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(columnName = COLUMN_ITEMID, foreign = true, foreignAutoRefresh = true)
	Item item;

	// only local
	@DatabaseField
	String hostNames;

	public Trigger() {

	}

	public Trigger(long id, String description, String expression,
			String comments, long lastChange, TriggerSeverity priority,
			int status, int value, String url) {
		this.id = id;
		this.description = description;
		this.expression = expression;
		this.comments = comments;
		this.lastChange = lastChange;
		this.priority = priority;
		this.status = status;
		this.value = value;
		this.url = url;
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

	public TriggerSeverity getPriority() {
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

	public long getLastChange() {
		return lastChange;
	}

	public Item getItem() {
		return item;
	}

	public String getHostNames() {
		return hostNames;
	}

	public void setHostNames(String hostNames) {
		this.hostNames = hostNames;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setPriority(TriggerSeverity priority) {
		this.priority = priority;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setUrl(String url) {
		this.url = url;
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
		sb.append(", ").append("lastchange=")
				.append(dateFormatter.format(lastChange));
		sb.append(", ").append("priority=").append(priority);
		sb.append(", ").append("status=").append(status);
		sb.append(", ").append("value=").append(value);
		sb.append(", ").append("url=").append(url);
		return sb.toString();
	}

	@Override
	public int compareTo(Trigger another) {
		if (id == another.getId())
			return 0;
		if (lastChange < another.getLastChange())
			return 1;
		if(item.getId() < another.getItem().getId())
			return 1;
		else
			return -1;
	}

}
