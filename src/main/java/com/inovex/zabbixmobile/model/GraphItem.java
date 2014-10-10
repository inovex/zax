package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "graphitems")
public class GraphItem {

	/** GraphItem ID- original gitemid */
	public static final String COLUMN_GRAPHITEMID = "graphitemid";
	@DatabaseField(id = true, unique = true, columnName = COLUMN_GRAPHITEMID)
	long id;
	/** Item ID */
	public static final String COLUMN_ITEMID = "itemid";
	@DatabaseField(columnName = COLUMN_ITEMID, foreign = true, foreignAutoRefresh = true)
	Item item;
	/** graph id */
	public static final String COLUMN_GRAPHID = "graphid";
	@DatabaseField(foreign = true, columnName = COLUMN_GRAPHID)
	Graph graph;
	/** color hex (only local, original as string) */
	public static final String COLUMN_COLOR = "color";
	@DatabaseField(columnName = COLUMN_COLOR)
	int color;

	long itemId;

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

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

}
