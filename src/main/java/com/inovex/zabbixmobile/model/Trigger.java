/*
This file is part of ZAX.

	ZAX is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	ZAX is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with ZAX.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.inovex.zabbixmobile.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;

import com.inovex.zabbixmobile.R;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "triggers")
public class Trigger implements Comparable<Trigger>, Sharable {

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
	public static final String COLUMN_ENABLED = "enabled";
	@DatabaseField(columnName = COLUMN_ENABLED)
	boolean enabled;

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

	@Override
	public int compareTo(Trigger another) {
		if (id == another.getId())
			return 0;
		if (lastChange < another.getLastChange())
			return 1;
		if (lastChange > another.getLastChange())
			return -1;
		if (item == null)
			return 1;
		if (another.getItem() == null)
			return -1;
		if (item.getId() > another.getItem().getId())
			return 1;
		else
			return -1;
	}

	public String getComments() {
		return comments;
	}

	public String getDescription() {
		String desc = description;
		if (hostNames != null && hostNames.length() > 0)
			desc = desc.replaceAll("\\{HOSTNAME\\}", hostNames);
		desc = desc.replaceAll("&nbsp;", " ");
		return desc;
	}

	public String getExpression() {
		String expr = expression;
		expr = expr.replaceAll("&nbsp;", " ");
		return expr;
	}

	public String getHostNames() {
		return hostNames;
	}

	public long getId() {
		return id;
	}

	public Item getItem() {
		return item;
	}

	public long getLastChange() {
		return lastChange;
	}

	public TriggerSeverity getPriority() {
		return priority;
	}

	public int getStatus() {
		return status;
	}

	public String getUrl() {
		return url;
	}

	public int getValue() {
		return value;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setHostNames(String hostNames) {
		this.hostNames = hostNames;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	public void setPriority(TriggerSeverity priority) {
		this.priority = priority;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("description=").append(getDescription());
		sb.append(", ").append("expression=").append(getExpression());
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
	public String getSharableString(Context context) {
		StringBuilder sb = new StringBuilder();
		Resources res = context.getResources();
		sb.append(res.getString(R.string.trigger) + ":\n");
		sb.append("\t" + res.getString(R.string.host) + ": " + hostNames + "\n");
		sb.append("\t" + res.getString(R.string.trigger) + ": "
				+ getDescription() + "\n");
		sb.append("\t" + res.getString(R.string.severity) + ": "
				+ res.getString(priority.getNameResourceId()) + "\n");
		sb.append("\t" + res.getString(R.string.expression) + ": "
				+ getExpression() + "\n");

		if (item != null) {
			sb.append(item.getSharableString(context));
		}

		return sb.toString();
	}

}
