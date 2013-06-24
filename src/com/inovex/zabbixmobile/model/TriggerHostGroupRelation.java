package com.inovex.zabbixmobile.model;

import com.j256.ormlite.field.DatabaseField;

public class TriggerHostGroupRelation {

	@DatabaseField(generatedId = true)
	private long id;
	/** Trigger ID */
	public static final String COLUMN_TRIGGERID = "triggerid";
	@DatabaseField(uniqueIndexName = "trigger_group_idx", foreign = true, columnName = COLUMN_TRIGGERID)
	private Trigger trigger;
	/** Host group ID */
	public static final String COLUMN_GROUPID = "groupid";
	@DatabaseField(uniqueIndexName = "trigger_group_idx", foreign = true, columnName = COLUMN_GROUPID)
	private HostGroup group;
	
	public TriggerHostGroupRelation() {
		
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public HostGroup getGroup() {
		return group;
	}

	public void setGroup(HostGroup group) {
		this.group = group;
	}
	
	
}
