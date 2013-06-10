package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;

public class Host {

	// TODO: For the moment, we have the id generated; in the real application,
	// we'll get it from Zabbix
	// @DatabaseField(id = true)
	@DatabaseField(generatedId = true)
	long id;
	@DatabaseField
	String host;
	
	// TODO: add group ID if necessary
	
	public Host() {
		
	}
	
	public Host(long id, String name) {
		//this.id = id;
		this.host = name;
	}
}
