package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.TriggerSeverity;

public interface OnSeverityListAdapterFilledListener {

	public void onSeverityListAdapterFilled(TriggerSeverity severity, boolean hostGroupChanged);
}
