package com.inovex.zabbixmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inovex.zabbixmobile.push.PushService;

public class OnUpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("UpdateBroadcastReceiver", "onUpdate");
		PushService.killPushService(context);
		PushService.startOrStopPushService(context, false);
	}

}
