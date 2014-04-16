package com.inovex.zabbixmobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnSettingsMigratedReceiver extends BroadcastReceiver {

	public static final String ACTION = "OnSettingsMigratedReceiver";
	private final Runnable mOnreceive;

	public OnSettingsMigratedReceiver(Runnable onreceive) {
		mOnreceive = onreceive;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("OnSettingsMigratedReceiver", "--- on settings migrated ---");
		if (mOnreceive != null) {
			mOnreceive.run();
		}
	}

}
