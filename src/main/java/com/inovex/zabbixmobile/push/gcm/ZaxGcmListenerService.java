package com.inovex.zabbixmobile.push.gcm;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.inovex.zabbixmobile.push.NotificationService;

/**
 * Created by felix on 16/10/15.
 */
public class ZaxGcmListenerService extends GcmListenerService {

	private static final String TAG = "ZaxGcmListener";

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Intent intent = new Intent(getApplicationContext(),NotificationService.class);
		intent.putExtra("status",data.getString("status"));
		intent.putExtra("message",data.getString("message"));
		intent.putExtra("triggerid", Long.parseLong(data.getString("triggerid")));
		startService(intent);
	}
}
