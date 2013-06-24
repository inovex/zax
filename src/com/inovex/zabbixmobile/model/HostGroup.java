package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;

public class HostGroup implements Comparable {
	
	public static final int GROUP_ID_ALL = -1;

	/** Host ID */
	public static final String COLUMN_GROUPID = "groupid";
	@DatabaseField(id = true, columnName = COLUMN_GROUPID)
	private long groupId;
	/** Host name */
	public static final String COLUMN_NAME = "name";
	@DatabaseField(columnName = COLUMN_NAME)
	private String name;
	
	public HostGroup() {
		
	}
	
	public HostGroup(long groupId, String name) {
		this.groupId = groupId;
		this.name = name;
	}
	
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Object another) {
		HostGroup anotherHostGroup = (HostGroup) another;
		if(groupId > anotherHostGroup.getGroupId())
			return 1;
		if(groupId < anotherHostGroup.getGroupId())
			return -1;
		return 0;
	}
	
}
