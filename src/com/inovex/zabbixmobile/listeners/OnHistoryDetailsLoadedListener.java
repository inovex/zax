package com.inovex.zabbixmobile.listeners;

import java.util.Collection;

import com.inovex.zabbixmobile.model.HistoryDetail;

public interface OnHistoryDetailsLoadedListener {

	public void onHistoryDetailsLoaded(Collection<HistoryDetail> historyDetails);
	
}
