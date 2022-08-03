package com.inovex.zabbixmobile.push.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by felix on 12/10/15.
 */
public class ListenerService extends InstanceIDListenerService{

	@Override
	public void onTokenRefresh() {
		Intent intent = new Intent(this,RegistrationIntentService.class);
		startService(intent);
	}
}
