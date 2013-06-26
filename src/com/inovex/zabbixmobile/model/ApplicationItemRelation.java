package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;

public class ApplicationItemRelation {
	public final static String TABLE_NAME = "applicationitemrelations";

	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField
	private long itemId;
	@DatabaseField
	private long applicationId;
	
//	@DatabaseField
//	private long hostId;

	public ApplicationItemRelation() {
		
	}
	
	public ApplicationItemRelation(long itemId, long applicationId) {
		this.itemId = itemId;
		this.applicationId = applicationId;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(long applicationId) {
		this.applicationId = applicationId;
	}
}
