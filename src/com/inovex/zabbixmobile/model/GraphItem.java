package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "graphitems")
public class GraphItem {

	/** GraphItem ID- original gitemid */
	public static final String COLUMN_GRAPHITEMID = "graphitemid";
	@DatabaseField(id = true, columnName = COLUMN_GRAPHITEMID)
	long id;
	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(columnName = COLUMN_ITEMID)
	long itemId;
	/** graph id */
	public static final String COLUMN_GRAPHID = "graphid";
	@DatabaseField(columnName = COLUMN_GRAPHID)
	long graphId;
	/** color hex (only local, original as string) */
	public static final String COLUMN_COLOR = "color";
	int color;

	public GraphItem() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public long getGraphId() {
		return graphId;
	}

	public void setGraphId(long graphId) {
		this.graphId = graphId;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

}
