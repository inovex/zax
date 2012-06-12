package com.inovex.zabbixmobile.activities.support;

import com.inovex.zabbixmobile.activities.MainActivityTablet.AppView;

public interface CurrentViewSupport {
	public AppView getCurrentView();
	public void setCurrentView(AppView view);
}
