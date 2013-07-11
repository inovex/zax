package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "historydetails")
public class HistoryDetail implements Comparable<HistoryDetail> {

	@DatabaseField(generatedId = true, columnName = "id")
	private long id;

	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(columnName = COLUMN_ITEMID, index = true)
	private long itemId;

	/** Unix timestamp */
	public static final String COLUMN_CLOCK = "clock";
	@DatabaseField(columnName = COLUMN_CLOCK)
	private long clock;

	/** Item value */
	public static final String COLUMN_VALUE = "value";
	@DatabaseField(columnName = COLUMN_VALUE)
	private double value;

	public HistoryDetail() {

	}

	public long getItemId() {
		return itemId;
	}

	public long getClock() {
		return clock;
	}

	public double getValue() {
		return value;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public void setClock(long clock) {
		this.clock = clock;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return itemId + " " + value + " " + clock;
	}

	@Override
	public int compareTo(HistoryDetail another) {
		if (itemId == another.getItemId()) {
			if (clock < another.getClock())
				return -1;
			return 1;
		}
		if (itemId < another.getItemId())
			return -1;
		return 1;
	}

}
