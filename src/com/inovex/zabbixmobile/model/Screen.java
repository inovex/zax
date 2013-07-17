package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "screens")
public class Screen {

	/** Screen ID */
	public static final String COLUMN_SCREENID = "screenid";
	@DatabaseField(id = true, columnName = COLUMN_SCREENID)
	long id;
	/** Screen name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	String name;
	
	public Screen() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(long screenId) {
		this.id = screenId;
	}

	public void setName(String name) {
		this.name = name;
	}

}
