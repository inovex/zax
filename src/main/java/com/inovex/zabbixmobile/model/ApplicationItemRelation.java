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

@DatabaseTable(tableName = "application_item_relation")
public class ApplicationItemRelation {

	@DatabaseField(generatedId = true)
	private long id;
	public static final String COLUMN_APPLICATIONID = "applicationid";
	@DatabaseField(uniqueIndexName = "app_item_idx", foreign = true, columnName = COLUMN_APPLICATIONID)
	private Application application;
	public static final String COLUMN_HOSTID = "hostid";
	@DatabaseField(foreign = true, columnName = COLUMN_HOSTID)
	private Host host;
	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(uniqueIndexName = "app_item_idx", foreign = true, foreignAutoRefresh = true, columnName = COLUMN_ITEMID)
	private Item item;

	// @DatabaseField
	// private long hostId;

	public ApplicationItemRelation() {

	}

	public ApplicationItemRelation(Application app, Host host, Item item) {
		this.application = app;
		this.host = host;
		this.item = item;
	}

	public long getId() {
		return id;
	}

	public Application getApplication() {
		return application;
	}

	public Item getItem() {
		return item;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void setItem(Item item) {
		this.item = item;
	}

}
