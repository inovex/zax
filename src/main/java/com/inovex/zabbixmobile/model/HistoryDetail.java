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
		if (clock < another.getClock())
			return -1;
		return 1;
	}

}
