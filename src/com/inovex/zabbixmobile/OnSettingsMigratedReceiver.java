package com.inovex.zabbixmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnSettingsMigratedReceiver extends BroadcastReceiver {

	public static final String ACTION = "OnSettingsMigratedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("OnSettingsMigratedReceiver", "--- on settings migrated ---");

	}

}
