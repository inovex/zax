package com.inovex.zabbixmobile.listeners;

import com.inovex.zabbixmobile.model.TriggerSeverity;

public interface OnSeverityListAdapterLoadedListener {

	public void onSeverityListAdapterLoaded(TriggerSeverity severity, boolean hostGroupChanged);
}
