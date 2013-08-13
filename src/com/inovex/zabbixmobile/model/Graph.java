package com.inovex.zabbixmobile.model;

import java.util.Collection;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "graphs")
public class Graph {

	/** Graph ID */
	public static final String COLUMN_GRAPHID = "graphid";
	@DatabaseField(id = true, columnName = COLUMN_GRAPHID)
	long id;
	/** Graph name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	String name;

	public static final String COLUMN_GRAPH_ITEMS = "graphitems";
	@ForeignCollectionField(eager = true, columnName = COLUMN_GRAPH_ITEMS)
	ForeignCollection<GraphItem> graphItems;

	public Graph() {

	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Collection<GraphItem> getGraphItems() {
		return graphItems;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
