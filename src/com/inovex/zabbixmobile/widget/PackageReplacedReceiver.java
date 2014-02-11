package com.inovex.zabbixmobile.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This broadcast receiver listens for replacements of the application package.
 * If the package receives an update, widget update alarm needs to be set again.
 * 
 */
public class PackageReplacedReceiver extends BroadcastReceiver {

	private static final String TAG = PackageReplacedReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "package replaced. Sending widget update broadcast.");
		Intent broadcastIntent = new Intent(context,
				WidgetUpdateBroadcastReceiver.class);
		broadcastIntent.putExtra(WidgetUpdateBroadcastReceiver.REFRESH_RATE_CHANGED,
				true);
		context.sendBroadcast(broadcastIntent);
	}

}
