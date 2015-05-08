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

@DatabaseTable(tableName = "zabbixservers")
public class ZabbixServer implements Comparable<ZabbixServer> {

	/** Screen ID */
	public static final String COLUMN_ZABBIXSERVERID = "zabbixserverid";
	@DatabaseField(columnName = COLUMN_ZABBIXSERVERID, generatedId = true)
	long id;

	/** Screen name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	String name;

	public ZabbixServer() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(long serverId) {
		this.id = serverId;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(ZabbixServer another) {
		return this.getName().compareTo(another.getName());
	}

}
