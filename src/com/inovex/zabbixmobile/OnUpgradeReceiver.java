package com.inovex.zabbixmobile;

import com.inovex.zabbixmobile.push.PushService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnUpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("UpdateBroadcastReceiver", "onUpdate");
		PushService.stopPushServiceAlarm(context);
		PushService.killPushService(context);
		PushService.startOrStopPushService(context);
	}

}
