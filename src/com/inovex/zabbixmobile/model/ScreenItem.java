package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "screenitems")
public class ScreenItem {

	/** screen Item ID */
	public static final String COLUMN_SCREENITEMID = "screenitemid";
	@DatabaseField(id = true, columnName = COLUMN_SCREENITEMID)
	long id;
	/** screen ID */
	public static final String COLUMN_SCREENID = "screenid";
	@DatabaseField(columnName = COLUMN_SCREENID, index = true)
	long screenId;
	/** graph id */
	public static final String COLUMN_RESOURCEID = "resourceid";
	@DatabaseField(columnName = COLUMN_RESOURCEID)
	long resourceId;
	
	public ScreenItem() {
		
	}
	
	public long getId() {
		return id;
	}
	public long getScreenId() {
		return screenId;
	}
	public long getResourceId() {
		return resourceId;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setScreenId(long screenId) {
		this.screenId = screenId;
	}
	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

}
